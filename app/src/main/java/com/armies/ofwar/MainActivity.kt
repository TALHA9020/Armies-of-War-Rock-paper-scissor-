package com.armies.ofwar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
            var selectedAttacker by remember { mutableStateOf<Outpost?>(null) }
            var deployMode by remember { mutableStateOf(false) }

            MaterialTheme {
                // 5: بیک گراؤنڈ اب گہرا نیلا (Space) ہے
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF000814)) {
                    when (gameState) {
                        "SETUP" -> SetupUI { color, count ->
                            engine.setupGame(count, color)
                            gameState = "MAP"
                        }
                        "MAP" -> MapScreen(
                            engine = engine,
                            onPostClick = { post ->
                                if (deployMode && post.ownerId == engine.currentTurnId.value) {
                                    engine.deployUnits(post.id, post.ownerId)
                                } else if (post.ownerId == 0) {
                                    selectedAttacker = post
                                } else if (selectedAttacker != null) {
                                    gameState = "BATTLE"
                                }
                            },
                            onDeployToggle = { deployMode = !deployMode },
                            isDeploying = deployMode
                        )
                        "BATTLE" -> BattleArenaUI { gameState = "MAP" }
                    }
                }
            }
        }
    }
}

@Composable
fun MapScreen(engine: BattleEngine, onPostClick: (Outpost) -> Unit, onDeployToggle: () -> Unit, isDeploying: Boolean) {
    val armies by engine.armies.collectAsState()
    val turnId by engine.currentTurnId.collectAsState()
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(Modifier.fillMaxSize()) {
        // نقشہ
        Canvas(Modifier.fillMaxSize().graphicsLayer(scaleX = scale, scaleY = scale, translationX = offset.x, translationY = offset.y)) {
            armies.forEach { army ->
                army.outposts.forEach { post ->
                    drawCircle(color = army.color, radius = 40f, center = Offset(post.posX, post.posY))
                }
            }
        }

        // 2: ہر پوسٹ کے اوپر یونٹس کی تفصیل (R P S)
        armies.forEach { army ->
            army.outposts.forEach { post ->
                Box(Modifier.offset((post.posX / 3).dp, (post.posY / 3).dp).clickable { onPostClick(post) }) {
                    Text("R:${post.units.rocks} P:${post.units.papers} S:${post.units.scissors}", 
                         color = Color.White, fontSize = 9.sp, modifier = Modifier.background(Color.Black.copy(0.5f)))
                }
            }
        }

        // 3 & 4: ڈپلائے اور ٹرن کنٹرولز
        Column(Modifier.align(Alignment.BottomCenter).padding(20.dp)) {
            Text("باری: ${armies.find { it.id == turnId }?.name ?: ""}", color = Color.Cyan)
            Row {
                Button(onClick = onDeployToggle, colors = ButtonDefaults.buttonColors(containerColor = if(isDeploying) Color.Green else Color.Gray)) {
                    Text(if(isDeploying) "ڈپلائنگ..." else "یونٹس لگائیں")
                }
                Spacer(Modifier.width(10.dp))
                Button(onClick = { engine.endTurn() }) { Text("باری ختم کریں") }
            }
        }
    }
}

@Composable
fun SetupUI(onStart: (Color, Int) -> Unit) {
    var selectedColor by remember { mutableStateOf(Color.Cyan) }
    var count by remember { mutableStateOf(2f) }
    val colors = listOf(Color.Cyan, Color.Red, Color.Green, Color.Yellow, Color.Magenta, Color.Blue, Color.White, Color.Gray, Color(0xFFFFA500), Color(0xFF4CAF50))

    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("اپنی آرمی کا رنگ چنیں (10 آپشنز)", color = Color.White, fontSize = 20.sp)
        // 1: رنگ منتخب کرنے کا اختیار
        Row(Modifier.padding(10.dp).horizontalScroll(rememberScrollState())) {
            colors.forEach { color ->
                Box(Modifier.size(40.dp).background(color, CircleShape)
                    .border(if(selectedColor == color) 3.dp else 0.dp, Color.White, CircleShape)
                    .clickable { selectedColor = color })
                Spacer(Modifier.width(8.dp))
            }
        }
        Slider(value = count, onValueChange = { count = it }, valueRange = 2f..10f, modifier = Modifier.padding(20.dp))
        Button(onClick = { onStart(selectedColor, count.toInt()) }) { Text("کائنات فتح کریں") }
    }
}

@Composable
fun BattleArenaUI(onExit: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
        Column {
            Text("جنگ کا میدان!", color = Color.Red, fontSize = 30.sp)
            Button(onClick = onExit) { Text("واپس") }
        }
    }
}
