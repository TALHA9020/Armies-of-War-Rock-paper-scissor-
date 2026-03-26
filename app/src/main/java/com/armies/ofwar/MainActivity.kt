package com.armies.ofwar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private val battleEngine = BattleEngine()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var gameState by remember { mutableStateOf("SETUP") }
            var selectedColor by remember { mutableStateOf(Color.Cyan) }
            var enemyCount by remember { mutableStateOf(2) }

            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF0F0F0F)) {
                    if (gameState == "SETUP") {
                        SetupScreen(onStart = { color, count ->
                            selectedColor = color
                            enemyCount = count
                            battleEngine.setupGame(count + 1, color)
                            gameState = "GAME"
                        })
                    } else {
                        GameView(battleEngine)
                    }
                }
            }
        }
    }
}

@Composable
fun SetupScreen(onStart: (Color, Int) -> Unit) {
    var color by remember { mutableStateOf(Color.Cyan) }
    var count by remember { mutableStateOf(3f) }
    val colors = listOf(Color.Cyan, Color.Red, Color.Green, Color.Yellow, Color.Magenta, Color.Blue)

    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("اپنی آرمی کا رنگ چنیں", color = Color.White, fontSize = 24.sp)
        Spacer(Modifier.height(20.dp))
        Row {
            colors.forEach { c ->
                Box(Modifier.size(45.dp).background(c, CircleShape)
                    .border(if (color == c) 4.dp else 0.dp, Color.White, CircleShape)
                    .clickable { color = c }
                )
                Spacer(Modifier.width(10.dp))
            }
        }
        Spacer(Modifier.height(40.dp))
        Text("دشمنوں کی تعداد: ${count.toInt()}", color = Color.White)
        Slider(value = count, onValueChange = { count = it }, valueRange = 2f..10f, modifier = Modifier.padding(horizontal = 30.dp))
        Spacer(Modifier.height(30.dp))
        Button(onClick = { onStart(color, count.toInt()) }, modifier = Modifier.fillMaxWidth(0.7f)) {
            Text("میدانِ جنگ میں داخل ہوں")
        }
    }
}

@Composable
fun GameView(engine: BattleEngine) {
    val armies by engine.armies.collectAsState()
    val turnId by engine.currentTurnId.collectAsState()
    var activeWave by remember { mutableStateOf<WaveData?>(null) }

    Box(Modifier.fillMaxSize()) {
        // نقشہ اور چوکیاں
        Canvas(modifier = Modifier.fillMaxSize()) {
            armies.forEach { army ->
                army.outposts.forEach { outpost ->
                    // چوکی کا دائرہ
                    drawCircle(color = army.color, radius = 40f, center = Offset(outpost.posX, outpost.posY))
                    drawCircle(color = Color.White, radius = 40f, center = Offset(outpost.posX, outpost.posY), style = Stroke(2f))
                }
            }
        }

        // یونٹس کا ڈیٹا دکھانا
        armies.forEach { army ->
            army.outposts.forEach { outpost ->
                Box(Modifier.offset(x = (outpost.posX / 2.7f).dp, y = (outpost.posY / 2.7f).dp)) {
                    Column(Modifier.background(Color.Black.copy(0.6f)).padding(2.dp)) {
                        Text("R:${outpost.units.rocks} P:${outpost.units.papers} S:${outpost.units.scissors}", color = Color.White, fontSize = 9.sp)
                    }
                }
            }
        }

        // باری کی اطلاع
        Text(if (turnId == 0) "آپ کی باری ہے" else "دشمن حملہ کر رہا ہے...", 
            Modifier.align(Alignment.TopCenter).padding(top = 40.dp), color = Color.White, fontSize = 18.sp)

        // لہر کی اینیمیشن
        activeWave?.let { wave ->
            WaveVisual(wave, onComplete = {
                engine.handleWaveConclusion(wave.ownerId, true)
                activeWave = null
            })
        }

        // کنٹرول بٹنز
        if (turnId == 0 && activeWave == null) {
            Row(Modifier.align(Alignment.BottomCenter).padding(bottom = 50.dp)) {
                GameButton("R", Color(0xFF8B4513)) { activeWave = WaveData(0, Color(0xFF8B4513), UnitType.ROCK) }
                GameButton("P", Color.LightGray) { activeWave = WaveData(0, Color.LightGray, UnitType.PAPER) }
                GameButton("S", Color.Yellow) { activeWave = WaveData(0, Color.Yellow, UnitType.SCISSORS) }
                Spacer(Modifier.width(10.dp))
                Button(onClick = { engine.endTurn() }) { Text("Done") }
            }
        }
    }
    
    // AI باری مینیج کرنا
    LaunchedEffect(turnId) {
        if (turnId != 0) {
            delay(2000)
            val enemyUnit = listOf(UnitType.ROCK, UnitType.PAPER, UnitType.SCISSORS).random()
            activeWave = WaveData(turnId, Color.Red, enemyUnit)
            delay(2000)
            engine.endTurn()
        }
    }
}

@Composable
fun GameButton(text: String, color: Color, onClick: () -> Unit) {
    Button(onClick = onClick, colors = ButtonDefaults.buttonColors(containerColor = color), 
        modifier = Modifier.padding(4.dp), contentPadding = PaddingValues(0.dp)) {
        Text(text, color = if(color == Color.Yellow) Color.Black else Color.White)
    }
}

@Composable
fun WaveVisual(wave: WaveData, onComplete: () -> Unit) {
    val progress = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        // لہر کا پھیلنا اور پھر واپس مڑنا (0 -> 1 -> 0)
        progress.animateTo(1f, animationSpec = tween(1000, easing = LinearEasing))
        progress.animateTo(0f, animationSpec = tween(800, easing = LinearEasing))
        onComplete()
    }

    Canvas(Modifier.fillMaxSize()) {
        val radius = progress.value * 1500f
        drawCircle(
            color = wave.color,
            radius = radius,
            center = center,
            style = Stroke(width = 15f),
            alpha = progress.value.coerceIn(0f, 0.8f)
        )
    }
}

data class WaveData(val ownerId: Int, val color: Color, val type: UnitType)
