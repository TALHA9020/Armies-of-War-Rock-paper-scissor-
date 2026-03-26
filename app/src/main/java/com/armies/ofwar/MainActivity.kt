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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    private val engine = BattleEngine()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var gameState by remember { mutableStateOf("SETUP") }
            var appMode by remember { mutableStateOf("DEPLOY") } // DEPLOY, ATTACK
            var selectedType by remember { mutableStateOf(UnitType.ROCK) }
            var attackerPost by remember { mutableStateOf<Outpost?>(null) }

            Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF00050F)) {
                when (gameState) {
                    "SETUP" -> SetupUI { color, count -> engine.setupGame(count, color); gameState = "MAP" }
                    "MAP" -> MapScreen(
                        engine = engine,
                        mode = appMode,
                        selectedUnitType = selectedType,
                        onPostClick = { post ->
                            if (appMode == "DEPLOY") {
                                engine.deploySpecific(post.id, selectedType)
                            } else {
                                if (attackerPost == null && post.ownerId == 0) {
                                    attackerPost = post
                                } else if (attackerPost != null && post.ownerId != 0) {
                                    gameState = "BATTLE_INTERFACE"
                                }
                            }
                        },
                        onModeChange = { appMode = it },
                        onTypeChange = { selectedType = it }
                    )
                    "BATTLE_INTERFACE" -> BattleArenaUI { gameState = "MAP"; attackerPost = null }
                }
            }
        }
    }
}

@Composable
fun MapScreen(engine: BattleEngine, mode: String, selectedUnitType: UnitType, 
              onPostClick: (Outpost) -> Unit, onModeChange: (String) -> Unit, onTypeChange: (UnitType) -> Unit) {
    val armies by engine.armies.collectAsState()
    var offset by remember { mutableStateOf(Offset.Zero) }
    
    // 1: ٹچ سکرول لاجک
    Box(Modifier.fillMaxSize().pointerInput(Unit) {
        detectTransformGestures { _, pan, _, _ -> offset += pan }
    }) {
        Canvas(Modifier.fillMaxSize().graphicsLayer(translationX = offset.x, translationY = offset.y)) {
            armies.forEach { army ->
                army.outposts.forEach { post ->
                    // 2: یونٹ کے حساب سے دائرے کا سائز
                    val radius = 50f + (post.units.total * 2f)
                    drawCircle(color = army.color, radius = radius, center = Offset(post.posX, post.posY))
                    drawCircle(color = Color.White.copy(0.2f), radius = radius + 5, center = Offset(post.posX, post.posY), style = androidx.compose.ui.graphics.drawscope.Stroke(2f))
                }
            }
        }

        // 2: نمبر بالکل دائرے کے اندر
        armies.forEach { army ->
            army.outposts.forEach { post ->
                Box(Modifier.offset((post.posX + offset.x / 2.5f).dp, (post.posY + offset.y / 2.5f).dp)
                    .clickable { onPostClick(post) }, contentAlignment = Alignment.Center) {
                    Text("R${post.units.rocks} P${post.units.papers} S${post.units.scissors}", 
                         color = Color.White, fontSize = 10.sp, modifier = Modifier.background(Color.Black.copy(0.5f)))
                }
            }
        }

        // 3 & 4: ڈپلائمنٹ اور اٹیک کنٹرولز
        Card(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(10.dp), colors = CardDefaults.cardColors(containerColor = Color.Black.copy(0.8f))) {
            Column(Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                if (mode == "DEPLOY") {
                    Row {
                        UnitBtn("Rock (${engine.rLeft.value})", UnitType.ROCK, selectedUnitType) { onTypeChange(it) }
                        UnitBtn("Paper (${engine.pLeft.value})", UnitType.PAPER, selectedUnitType) { onTypeChange(it) }
                        UnitBtn("Scissor (${engine.sLeft.value})", UnitType.SCISSORS, selectedUnitType) { onTypeChange(it) }
                    }
                    Button(onClick = { onModeChange("ATTACK") }, Modifier.fillMaxWidth()) { Text("اٹیک موڈ شروع کریں") }
                } else {
                    Text("اٹیک موڈ: پہلے اپنی پوسٹ پھر دشمن کی پوسٹ چنیں", color = Color.Red)
                    Button(onClick = { onModeChange("DEPLOY") }) { Text("واپس ڈپلائے پر جائیں") }
                }
            }
        }
    }
}

@Composable
fun UnitBtn(label: String, type: UnitType, current: UnitType, onClick: (UnitType) -> Unit) {
    Button(onClick = { onClick(type) }, 
           colors = ButtonDefaults.buttonColors(containerColor = if(type == current) Color.Cyan else Color.DarkGray),
           modifier = Modifier.padding(4.dp)) {
        Text(label, fontSize = 10.sp)
    }
}

@Composable
fun BattleArenaUI(onExit: () -> Unit) {
    // 5: R P S بٹنز کے ساتھ بیٹل فارمیشن
    Column(Modifier.fillMaxSize().background(Color.Black), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("فارمیشن سیٹ کریں اور حملہ کریں!", color = Color.White, fontSize = 20.sp)
        Spacer(Modifier.height(30.dp))
        Row {
            Button(onClick = { /* Rock Attack */ }) { Text("Rock") }
            Button(onClick = { /* Paper Attack */ }) { Text("Paper") }
            Button(onClick = { /* Scissor Attack */ }) { Text("Scissor") }
        }
        Button(onClick = onExit, Modifier.padding(top = 50.dp)) { Text("جنگ ختم کریں") }
    }
}

@Composable
fun SetupUI(onStart: (Color, Int) -> Unit) {
    var selColor by remember { mutableStateOf(Color.Cyan) }
    var enemies by remember { mutableStateOf(2f) }
    val colors = listOf(Color.Cyan, Color.Red, Color.Green, Color.Yellow, Color.Magenta, Color.Blue, Color.White, Color.Gray, Color(0xFFFFA500), Color(0xFF4CAF50))

    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("آرمی کا رنگ منتخب کریں", color = Color.White)
        Row(Modifier.horizontalScroll(rememberScrollState())) {
            colors.forEach { Box(Modifier.size(45.dp).background(it, CircleShape).border(if(selColor == it) 3.dp else 0.dp, Color.White, CircleShape).clickable { selColor = it }); Spacer(Modifier.width(8.dp)) }
        }
        Slider(value = enemies, onValueChange = { enemies = it }, valueRange = 2f..10f, modifier = Modifier.padding(20.dp))
        Button(onClick = { onStart(selColor, enemies.toInt()) }) { Text("گیم شروع کریں") }
    }
}
