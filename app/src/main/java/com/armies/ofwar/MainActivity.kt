package com.armies.ofwar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private val engine = BattleEngine()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var gameState by remember { mutableStateOf("SETUP") }
            var selectedAttacker by remember { mutableStateOf<Outpost?>(null) }
            var selectedTarget by remember { mutableStateOf<Outpost?>(null) }

            MaterialTheme {
                Surface(color = Color(0xFF050505)) {
                    when (gameState) {
                        "SETUP" -> SetupUI { color, count ->
                            engine.setupGame(count, color)
                            gameState = "MAP"
                        }
                        "MAP" -> GameMapView(
                            engine = engine,
                            onPostClick = { post ->
                                if (post.ownerId == 0) {
                                    selectedAttacker = post // اپنی پوسٹ سلیکٹ کریں
                                } else if (selectedAttacker != null) {
                                    if (engine.canInitiateAttack(selectedAttacker!!)) {
                                        selectedTarget = post // دشمن کو ٹارگٹ کریں
                                        gameState = "BATTLE"
                                    }
                                }
                            }
                        )
                        "BATTLE" -> BattleArenaUI(
                            attacker = selectedAttacker!!,
                            defender = selectedTarget!!,
                            onFinish = {
                                gameState = "MAP"
                                selectedAttacker = null
                                selectedTarget = null
                                engine.endTurn()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BattleArenaUI(attacker: Outpost, defender: Outpost, onFinish: () -> Unit) {
    var playerWave by remember { mutableStateOf(UnitType.NONE) }
    var aiWave by remember { mutableStateOf(UnitType.NONE) }

    // 14: AI کا خودکار فائر سسٹم
    LaunchedEffect(Unit) {
        while (true) {
            delay(1200) 
            aiWave = listOf(UnitType.ROCK, UnitType.PAPER, UnitType.SCISSORS).random()
        }
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("بیٹل فیز", color = Color.Red, fontSize = 24.sp)
            
            // 6, 7: ویو اینیمیشن
            WaveClashEffect(playerWave, aiWave)
            
            // 6: پلیئر اٹیک بٹنز
            Row(Modifier.fillMaxWidth().padding(20.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { playerWave = UnitType.ROCK }) { Text("Rock") }
                Button(onClick = { playerWave = UnitType.PAPER }) { Text("Paper") }
                Button(onClick = { playerWave = UnitType.SCISSORS }) { Text("Scissors") }
            }
            Button(onClick = onFinish) { Text("واپس میپ پر جائیں") }
        }
    }
}

@Composable
fun GameMapView(engine: BattleEngine, onPostClick: (Outpost) -> Unit) {
    val armies by engine.armies.collectAsState()
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale *= zoomChange
        offset += offsetChange
    }

    Box(Modifier.fillMaxSize().transformable(state = transformState)) {
        Canvas(Modifier.fillMaxSize().graphicsLayer(
            scaleX = scale, scaleY = scale,
            translationX = offset.x, translationY = offset.y
        )) {
            armies.forEach { army ->
                army.outposts.forEach { post ->
                    drawCircle(color = army.color, radius = 35f, center = Offset(post.posX, post.posY))
                }
            }
        }
        
        // 3: سکرول ایبل کلک ایبل ایریاز
        armies.forEach { army ->
            army.outposts.forEach { post ->
                Box(Modifier.offset(x = (post.posX / 3).dp, y = (post.posY / 3).dp)
                    .size(40.dp)
                    .clickable { onPostClick(post) }
                )
            }
        }
    }
}

@Composable
fun WaveClashEffect(attacker: UnitType, defender: UnitType) {
    val anim = remember { Animatable(0f) }
    val result = RPSRules.resolve(attacker, defender)
    LaunchedEffect(attacker, defender) {
        anim.snapTo(0f)
        anim.animateTo(1f, tween(500))
    }
    Canvas(Modifier.size(100.dp)) {
        drawCircle(
            color = if (result == true) Color.Cyan else Color.Red,
            radius = anim.value * 100f,
            alpha = 1f - anim.value
        )
    }
}

@Composable
fun SetupUI(onStart: (Color, Int) -> Unit) {
    var count by remember { mutableStateOf(2f) }
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("آرمیز کی تعداد: ${count.toInt()}", color = Color.White)
        Slider(value = count, onValueChange = { count = it }, valueRange = 2f..10f, modifier = Modifier.padding(30.dp))
        Button(onClick = { onStart(Color.Cyan, count.toInt()) }) { Text("کائنات کا سفر شروع کریں") }
    }
}
