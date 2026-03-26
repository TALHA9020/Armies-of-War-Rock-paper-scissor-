package com.armies.ofwar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
            var mode by remember { mutableStateOf("IDLE") } // IDLE, DEPLOY, MOVE, ATTACK
            var firstSelectedPost by remember { mutableStateOf<Outpost?>(null) }

            Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF000B1E)) {
                when (gameState) {
                    "SETUP" -> SetupUI { color, count -> engine.setupGame(count, color); gameState = "MAP" }
                    "MAP" -> MapScreen(
                        engine = engine,
                        currentMode = mode,
                        onPostClick = { post ->
                            when (mode) {
                                "DEPLOY" -> engine.deployAt(post.id)
                                "MOVE" -> {
                                    if (firstSelectedPost == null) firstSelectedPost = post
                                    else {
                                        engine.moveUnits(firstSelectedPost!!.id, post.id)
                                        firstSelectedPost = null
                                    }
                                }
                                "ATTACK" -> { /* جنگ کی لاجک یہاں آئے گی */ }
                            }
                        },
                        onModeChange = { mode = it }
                    )
                }
            }
        }
    }
}

@Composable
fun MapScreen(engine: BattleEngine, currentMode: String, onPostClick: (Outpost) -> Unit, onModeChange: (String) -> Unit) {
    val armies by engine.armies.collectAsState()
    val turnId by engine.currentTurnId.collectAsState()
    val deployRemaining by engine.deployableUnits.collectAsState()
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(Modifier.fillMaxSize()) {
        // نقشہ (Stars Background Style)
        Canvas(Modifier.fillMaxSize().graphicsLayer(scaleX = scale, scaleY = scale, translationX = offset.x, translationY = offset.y)) {
            armies.forEach { army ->
                army.outposts.forEach { post ->
                    drawCircle(color = army.color, radius = 45f, center = Offset(post.posX, post.posY))
                }
            }
        }

        // 2: یونٹ کاؤنٹرز
        armies.forEach { army ->
            army.outposts.forEach { post ->
                Box(Modifier.offset((post.posX / 3).dp, (post.posY / 3).dp)
                    .background(Color.Black.copy(0.7f), CircleShape).padding(4.dp)
                    .clickable { onPostClick(post) }) {
                    Text("R:${post.units.rocks} P:${post.units.papers} S:${post.units.scissors}", color = Color.White, fontSize = 8.sp)
                }
            }
        }

        // کنٹرول پینل
        Card(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(16.dp), colors = CardDefaults.cardColors(containerColor = Color.Black.copy(0.9f))) {
            Column(Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("باری: ${armies.find { it.id == turnId }?.name ?: ""} | ڈپلائے باقی: $deployRemaining", color = Color.Cyan)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(onClick = { onModeChange("DEPLOY") }, colors = ButtonDefaults.buttonColors(containerColor = if(currentMode == "DEPLOY") Color.Green else Color.DarkGray)) { Text("ڈپلائے") }
                    Button(onClick = { onModeChange("MOVE") }, colors = ButtonDefaults.buttonColors(containerColor = if(currentMode == "MOVE") Color.Blue else Color.DarkGray)) { Text("منتقلی") }
                    Button(onClick = { engine.endTurn() }) { Text("باری ختم") }
                }
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
        Text("اپنی آرمی کا رنگ چنیں", color = Color.White, fontSize = 22.sp)
        Row(Modifier.padding(20.dp).horizontalScroll(rememberScrollState())) {
            colors.forEach { color ->
                Box(Modifier.size(50.dp).background(color, CircleShape)
                    .border(if(selectedColor == color) 4.dp else 0.dp, Color.White, CircleShape)
                    .clickable { selectedColor = color })
                Spacer(Modifier.width(10.dp))
            }
        }
        Text("دشمنوں کی تعداد: ${count.toInt()}", color = Color.White)
        Slider(value = count, onValueChange = { count = it }, valueRange = 2f..10f, Modifier.padding(30.dp))
        Button(onClick = { onStart(selectedColor, count.toInt()) }) { Text("گیم شروع کریں") }
    }
}
