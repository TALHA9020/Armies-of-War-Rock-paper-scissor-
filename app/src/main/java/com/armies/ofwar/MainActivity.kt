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
                Surface(color = Color.Black) {
                    when (gameState) {
                        "SETUP" -> SetupUI { color, count ->
                            engine.setupGame(count, color)
                            gameState = "MAP"
                        }
                        "MAP" -> GameMapView(engine)
                        "BATTLE" -> BattleArenaUI()
                    }
                }
            }
        }
    }
}

@Composable
fun GameMapView(engine: BattleEngine) {
    val armies by engine.armies.collectAsState()
    
    // 3: ٹچ سکرول اور زوم (ہزاروں پوسٹس کے لیے)
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
                    drawCircle(color = army.color, radius = 20f, center = Offset(post.posX, post.posY))
                }
            }
        }
    }
}

@Composable
fun BattleArenaUI() {
    // 6: ویو فائٹنگ بٹنز (R P S)
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Bottom) {
        Row(Modifier.fillMaxWidth().padding(20.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(onClick = { /* فائر راک ویو */ }) { Text("R") }
            Button(onClick = { /* فائر پیپر ویو */ }) { Text("P") }
            Button(onClick = { /* فائر سیزر ویو */ }) { Text("S") }
        }
    }
}

@Composable
fun SetupUI(onStart: (Color, Int) -> Unit) {
    // 1, 2: آرمی کا رنگ اور تعداد منتخب کرنے کی سکرین
    var selectedColor by remember { mutableStateOf(Color.Red) }
    var armyCount by remember { mutableStateOf(2f) }

    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("آرمی کا رنگ اور تعداد چنیں", color = Color.White, fontSize = 20.sp)
        Slider(value = armyCount, onValueChange = { armyCount = it }, valueRange = 2f..10f)
        Button(onClick = { onStart(selectedColor, armyCount.toInt()) }) {
            Text("کائنات کا سفر شروع کریں")
        }
    }
}
