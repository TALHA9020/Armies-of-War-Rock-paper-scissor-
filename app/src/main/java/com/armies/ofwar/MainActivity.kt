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

class MainActivity : ComponentActivity() {
    private val battleEngine = BattleEngine()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var gameState by remember { mutableStateOf("SETUP") } // SETUP, GAME
            var selectedColor by remember { mutableStateOf(Color.Cyan) }
            var enemyCount by remember { mutableStateOf(2) }

            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF121212)) {
                    if (gameState == "SETUP") {
                        SetupScreen(
                            onStart = { color, count ->
                                selectedColor = color
                                enemyCount = count
                                battleEngine.setupGame(count + 1, color)
                                gameState = "GAME"
                            }
                        )
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
    var count by remember { mutableStateOf(2f) }
    val colors = listOf(Color.Cyan, Color.Red, Color.Green, Color.Yellow, Color.Magenta)

    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("اپنی آرمی کا رنگ منتخب کریں", color = Color.White, fontSize = 20.sp)
        Row(Modifier.padding(16.dp)) {
            colors.forEach { c ->
                Box(Modifier.size(40.dp).background(c, CircleShape)
                    .border(if(color == c) 3.dp else 0.dp, Color.White, CircleShape)
                    .clickable { color = c }
                )
                Spacer(Modifier.width(8.dp))
            }
        }
        
        Text("دشمنوں کی تعداد: ${count.toInt()}", color = Color.White)
        Slider(value = count, onValueChange = { count = it }, valueRange = 2f..10f, modifier = Modifier.padding(16.dp))
        
        Button(onClick = { onStart(color, count.toInt()) }) {
            Text("جنگ شروع کریں!")
        }
    }
}

@Composable
fun GameView(engine: BattleEngine) {
    val armies by engine.armies.collectAsState()
    val turnId by engine.currentTurnId.collectAsState()
    var showWave by remember { mutableStateOf(false) }
    var waveColor by remember { mutableStateOf(Color.White) }

    Box(Modifier.fillMaxSize()) {
        // نقشہ (Map Layer)
        Canvas(modifier = Modifier.fillMaxSize()) {
            armies.forEach { army ->
                army.outposts.forEach { outpost ->
                    drawCircle(color = army.color, radius = 30f, center = Offset(outpost.posX, outpost.posY))
                }
            }
        }

        // چوکیوں کے اوپر یونٹس کی تعداد (UI Layer)
        armies.forEach { army ->
            army.outposts.forEach { outpost ->
                Box(Modifier.offset(x = (outpost.posX/3).dp, y = (outpost.posY/3).dp)) { // فرضی اسکیلنگ
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("R:${outpost.units.rocks} P:${outpost.units.papers} S:${outpost.units.scissors}", 
                            color = Color.White, fontSize = 10.sp, modifier = Modifier.background(Color.Black.copy(0.5f)))
                    }
                }
            }
        }

        // اٹیک/ڈیفنڈ لہریں (Wave Animation)
        if (showWave) {
            WaveEffect(waveColor) { showWave = false }
        }

        // کنٹرول بٹنز
        Column(Modifier.align(Alignment.BottomCenter).padding(16.dp)) {
            if (turnId == 0) {
                Row {
                    Button(onClick = { waveColor = Color.Red; showWave = true }) { Text("R") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { waveColor = Color.Blue; showWave = true }) { Text("P") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { waveColor = Color.Green; showWave = true }) { Text("S") }
                }
                Button(onClick = { engine.endTurn() }, Modifier.fillMaxWidth()) { Text("باری ختم کریں") }
            } else {
                Text("دشمن کی باری ہے...", color = Color.Red, fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun WaveEffect(color: Color, onFinished: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "")
    val radius by transition.animateFloat(
        initialValue = 0f, targetValue = 1000f,
        animationSpec = infiniteRepeatable(animation = tendency(1000), repeatMode = RepeatMode.Restart), label = ""
    )

    Canvas(Modifier.fillMaxSize()) {
        drawCircle(color = color, radius = radius, center = center, style = Stroke(width = 10f), alpha = (1f - radius/1000f))
    }
    
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1000)
        onFinished()
    }
}

fun tendency(duration: Int) = tween<Float>(durationMillis = duration, easing = LinearEasing)
