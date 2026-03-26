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
            var currentMode by remember { mutableStateOf("IDLE") } // DEPLOY, MOVE, ATTACK
            var moveSourcePost by remember { mutableStateOf<Outpost?>(null) }

            Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF000B1E)) {
                when (gameState) {
                    "SETUP" -> SetupUI { color, count ->
                        engine.setupGame(count, color)
                        gameState = "MAP"
                    }
                    "MAP" -> MapScreen(
                        engine = engine,
                        mode = currentMode,
                        onPostClick = { post ->
                            when (currentMode) {
                                "DEPLOY" -> engine.deployAt(post.id)
                                "MOVE" -> {
                                    if (moveSourcePost == null) moveSourcePost = post
                                    else {
                                        engine.moveUnits(moveSourcePost!!.id, post.id)
                                        moveSourcePost = null
                                    }
                                }
                            }
                        },
                        onModeChange = { currentMode = it }
                    )
                }
            }
        }
    }
}

@Composable
fun MapScreen(engine: BattleEngine, mode: String, onPostClick: (Outpost) -> Unit, onModeChange: (String) -> Unit) {
    val armies by engine.armies.collectAsState()
    val turnId by engine.currentTurnId.collectAsState()
    val deployLeft by engine.deployableUnits.collectAsState()
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(Modifier.fillMaxSize()) {
        // Starry Map Background
        Canvas(Modifier.fillMaxSize().graphicsLayer(scaleX = scale, scaleY = scale, translationX = offset.x, translationY = offset.y)) {
            armies.forEach { army ->
                army.outposts.forEach { post ->
                    drawCircle(color = army.color, radius = 45f, center = Offset(post.posX, post.posY))
                    drawCircle(color = Color.White.copy(0.3f), radius = 50f, center = Offset(post.posX, post.posY), style = androidx.compose.ui.graphics.drawscope.Stroke(2f))
                }
            }
        }

        // 2: یونٹس کی تفصیل (R P S)
        armies.forEach { army ->
            army.outposts.forEach { post ->
                Box(Modifier.offset((post.posX / 3).dp, (post.posY / 3).dp)
                    .background(Color.Black.copy(0.7f), CircleShape).padding(4.dp)
                    .clickable { onPostClick(post) }) {
                    Text("R:${post.units.rocks} P:${post.units.papers} S:${post.units.scissors}", color = Color.White, fontSize = 8.sp)
                }
            }
        }

        // 4: ٹرن اور کنٹرول پینل
        Card(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(16.dp), colors = CardDefaults.cardColors(containerColor = Color.Black.copy(0.9f))) {
            Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("باری: ${armies.find { it.id == turnId }?.name ?: ""} | ڈپلائے: $deployLeft", color = Color.Cyan)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(onClick = { onModeChange("DEPLOY") }, colors = ButtonDefaults.buttonColors(containerColor = if(mode == "DEPLOY") Color.Green else Color.DarkGray)) { Text("ڈپلائے") }
                    Button(onClick = { onModeChange("MOVE") }, colors = ButtonDefaults.buttonColors(containerColor = if(mode == "MOVE") Color.Blue else Color.DarkGray)) { Text("منتقلی") }
                    Button(onClick = { engine.endTurn() }) { Text("باری ختم") }
                }
            }
        }
    }
}

@Composable
fun SetupUI(onStart: (Color, Int) -> Unit) {
    var selectedCol by remember { mutableStateOf(Color.Cyan) }
    var enemyCount by remember { mutableStateOf(2f) }
    val colors = listOf(Color.Cyan, Color.Red, Color.Green, Color.Yellow, Color.Magenta, Color.Blue, Color.White, Color.Gray, Color(0xFFFFA500), Color(0xFF4CAF50))

    Column(Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("اپنی آرمی کا رنگ منتخب کریں", color = Color.White, fontSize = 22.sp)
        Row(Modifier.padding(20.dp).horizontalScroll(rememberScrollState())) {
            colors.forEach { color ->
                Box(Modifier.size(50.dp).background(color, CircleShape).border(if(selectedCol == color) 4.dp else 0.dp, Color.White, CircleShape).clickable { selectedCol = color })
                Spacer(Modifier.width(10.dp))
            }
        }
        Text("دشمنوں کی تعداد: ${enemyCount.toInt()}", color = Color.White)
        Slider(value = enemyCount, onValueChange = { enemyCount = it }, valueRange = 2f..10f)
        Button(onClick = { onStart(selectedCol, enemyCount.toInt()) }, modifier = Modifier.fillMaxWidth()) { Text("کائنات فتح کریں") }
    }
}
