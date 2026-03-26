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
            var enemyCount by remember { mutableStateOf(3) }
            
            var attackerOutpost by remember { mutableStateOf<Outpost?>(null) }
            var defenderOutpost by remember { mutableStateOf<Outpost?>(null) }

            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF0A0A0A)) {
                    when (gameState) {
                        "SETUP" -> SetupScreen { color, count ->
                            selectedColor = color
                            enemyCount = count
                            battleEngine.setupGame(count + 1, color)
                            gameState = "MAP"
                        }
                        "MAP" -> GameMapView(
                            engine = battleEngine,
                            onAttackInitiated = { attacker, defender ->
                                attackerOutpost = attacker
                                defenderOutpost = defender
                                gameState = "BATTLE"
                            }
                        )
                        "BATTLE" -> BattleArena(
                            attacker = attackerOutpost!!,
                            defender = defenderOutpost!!,
                            onBattleResult = { playerUnit, enemyUnit ->
                                battleEngine.executeBattle(attackerOutpost!!, defenderOutpost!!, playerUnit)
                                gameState = "MAP"
                                attackerOutpost = null
                                defenderOutpost = null
                            }
                        )
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
        Text(text = "اپنی آرمی کا رنگ منتخب کریں", color = Color.White, fontSize = 22.sp)
        Row(Modifier.padding(16.dp)) {
            colors.forEach { c ->
                Box(Modifier.size(45.dp).background(c, CircleShape)
                    .border(if (color == c) 4.dp else 0.dp, Color.White, CircleShape)
                    .clickable { color = c }
                )
                Spacer(Modifier.width(8.dp))
            }
        }
        Text(text = "دشمنوں کی تعداد: ${count.toInt()}", color = Color.White)
        Slider(value = count, onValueChange = { count = it }, valueRange = 2f..10f, modifier = Modifier.padding(20.dp))
        Button(onClick = { onStart(color, count.toInt()) }) { Text(text = "جنگ شروع کریں") }
    }
}

@Composable
fun GameMapView(engine: BattleEngine, onAttackInitiated: (Outpost, Outpost) -> Unit) {
    val armies by engine.armies.collectAsState()
    var selectedByPlayer by remember { mutableStateOf<Outpost?>(null) }

    Box(Modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) {
            armies.forEach { army ->
                army.outposts.forEach { outpost ->
                    drawCircle(color = army.color, radius = 45f, center = Offset(outpost.posX, outpost.posY))
                    if (selectedByPlayer?.id == outpost.id) {
                        drawCircle(color = Color.White, radius = 55f, center = Offset(outpost.posX, outpost.posY), style = Stroke(5f))
                    }
                }
            }
        }

        armies.forEach { army ->
            army.outposts.forEach { outpost ->
                Box(Modifier.fillMaxSize()) {
                    Box(
                        Modifier.offset(x = (outpost.posX / 3f).dp, y = (outpost.posY / 3f).dp)
                            .size(50.dp)
                            .clickable {
                                if (outpost.ownerId == 0) {
                                    selectedByPlayer = outpost
                                } else if (selectedByPlayer != null) {
                                    onAttackInitiated(selectedByPlayer!!, outpost)
                                    selectedByPlayer = null
                                }
                            }
                    )
                    Text(
                        text = "R:${outpost.units.rocks} P:${outpost.units.papers} S:${outpost.units.scissors}",
                        modifier = Modifier.offset(x = (outpost.posX / 3f).dp, y = (outpost.posY / 3f + 20).dp),
                        color = Color.White, 
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
fun BattleArena(attacker: Outpost, defender: Outpost, onBattleResult: (UnitType, UnitType) -> Unit) {
    var playerChoice by remember { mutableStateOf<UnitType?>(null) }
    var enemyChoice by remember { mutableStateOf<UnitType?>(null) }
    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(playerChoice) {
        if (playerChoice != null) {
            enemyChoice = listOf(UnitType.ROCK, UnitType.PAPER, UnitType.SCISSORS).random()
            animProgress.animateTo(1f, tween(1000))
            animProgress.animateTo(0f, tween(800))
            onBattleResult(playerChoice!!, enemyChoice!!)
        }
    }

    Box(Modifier.fillMaxSize().background(Color.Black.copy(0.85f)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "آمنے سامنے کا مقابلہ!", color = Color.Red, fontSize = 28.sp)
            Spacer(Modifier.height(40.dp))
            
            if (enemyChoice != null) {
                // یہاں ہم نے .name لگا دیا ہے تاکہ ایرر ختم ہو جائے
                Text(text = "دشمن کا دفاع: ${enemyChoice!!.name}", color = Color.White)
            }

            Box(Modifier.size(300.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    if (animProgress.value > 0) {
                        drawCircle(color = Color.White, radius = animProgress.value * 500f, style = Stroke(10f), alpha = 1f - animProgress.value)
                    }
                }
            }

            Text(text = "اپنا یونٹ چنیں اور حملہ کریں!", color = Color.White)
            Row {
                BattleButton("Rock", Color(0xFF8B4513)) { playerChoice = UnitType.ROCK }
                BattleButton("Paper", Color.White) { playerChoice = UnitType.PAPER }
                BattleButton("Scissors", Color.Yellow) { playerChoice = UnitType.SCISSORS }
            }
        }
    }
}

@Composable
fun BattleButton(label: String, color: Color, onClick: () -> Unit) {
    Button(onClick = onClick, colors = ButtonDefaults.buttonColors(containerColor = color), modifier = Modifier.padding(8.dp)) {
        Text(text = label, color = if(color == Color.White || color == Color.Yellow) Color.Black else Color.White)
    }
}
