package com.armies.ofwar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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

class MainActivity : ComponentActivity() {
    private val engine = BattleEngine()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var gameState by remember { mutableStateOf("SETUP") }
            MaterialTheme {
                Surface(color = Color(0xFF050505)) {
                    when (gameState) {
                        "SETUP" -> SetupUI { color, count ->
                            engine.setupGame(count, color)
                            gameState = "MAP"
                        }
                        "MAP" -> GameMapView(engine)
                    }
                }
            }
        }
    }
}

@Composable
fun GameMapView(engine: BattleEngine) {
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
                    drawCircle(color = army.color, radius = 30f, center = Offset(post.posX, post.posY))
                }
            }
        }
        
        // باری کی اطلاع اور کارڈز
        Column(Modifier.padding(16.dp).align(Alignment.TopStart)) {
            val currentArmy = armies.find { it.id == engine.currentTurnId.collectAsState().value }
            Text("باری: ${currentArmy?.name}", color = Color.White)
            Text("لیول: ${currentArmy?.outposts?.firstOrNull()?.level?.label}", color = Color.Yellow)
        }
    }
}

@Composable
fun WaveClashEffect(attackerUnit: UnitType, defenderUnit: UnitType) {
    val clashAnimation = remember { Animatable(0f) }
    val result = RPSRules.resolve(attackerUnit, defenderUnit)

    LaunchedEffect(Unit) {
        clashAnimation.animateTo(1f, tween(500))
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(150.dp)) {
            drawCircle(
                color = if (result == true) Color.Cyan else Color.Red,
                radius = clashAnimation.value * 150f,
                alpha = 1f - clashAnimation.value
            )
        }
    }
}

@Composable
fun SetupUI(onStart: (Color, Int) -> Unit) {
    var armyCount by remember { mutableStateOf(2f) }
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("آرمیز کی تعداد منتخب کریں: ${armyCount.toInt()}", color = Color.White)
        Slider(value = armyCount, onValueChange = { armyCount = it }, valueRange = 2f..10f, modifier = Modifier.padding(30.dp))
        Button(onClick = { onStart(Color.Cyan, armyCount.toInt()) }) { Text("سفر شروع کریں") }
    }
}
