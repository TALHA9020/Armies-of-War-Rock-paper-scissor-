package com.armies.ofwar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    private val battleEngine = BattleEngine()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var gameStarted by remember { mutableStateOf(false) }
            MaterialTheme {
                Surface(color = Color(0xFF1A1A1A)) {
                    if (!gameStarted) {
                        SetupScreen { count, allies ->
                            battleEngine.setupGame(count, allies)
                            gameStarted = true
                        }
                    } else {
                        BattleField(battleEngine)
                    }
                }
            }
        }
    }
}

@Composable
fun BattleField(engine: BattleEngine) {
    val armies by engine.armies.collectAsState()
    val phase by engine.currentPhase.collectAsState()
    val cards by engine.userCards.collectAsState()
    val turnId by engine.currentTurnId.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("PHASE: $phase", color = Color.Yellow, fontSize = 18.sp)
        Text("Cards: $cards / 4", color = Color.Cyan)

        if (cards >= 4) {
            Button(onClick = { engine.exchangeCards() }) { Text("Exchange Cards for 20 Units") }
        }

        Spacer(Modifier.height(20.dp))
        
        // فیز سوئچر
        if (turnId == 0) {
            Row {
                Button(onClick = { engine.setPhase(TurnPhase.ATTACK) }, 
                    colors = ButtonDefaults.buttonColors(containerColor = if(phase == TurnPhase.ATTACK) Color.Red else Color.Gray)) {
                    Text("ATTACK")
                }
                Spacer(Modifier.width(10.dp))
                Button(onClick = { engine.setPhase(TurnPhase.MOVE) },
                    colors = ButtonDefaults.buttonColors(containerColor = if(phase == TurnPhase.MOVE) Color.Blue else Color.Gray)) {
                    Text("MOVE")
                }
            }
        }

        Spacer(Modifier.weight(1f))

        if (turnId == 0) {
            Row {
                UnitButton("R") { engine.addUnitToWave(true, UnitType.ROCK) }
                UnitButton("P") { engine.addUnitToWave(true, UnitType.PAPER) }
                UnitButton("S") { engine.addUnitToWave(true, UnitType.SCISSORS) }
            }
            Button(onClick = { engine.endTurn() }, modifier = Modifier.padding(top = 10.dp)) {
                Text("END TURN")
            }
        } else {
            Text("Enemy is thinking...", color = Color.White)
        }
        
        Text("Your Units: ${armies.firstOrNull()?.armyCount ?: 0}", color = Color.White, modifier = Modifier.padding(10.dp))
    }
}

@Composable
fun UnitButton(label: String, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.padding(4.dp)) { Text(label) }
}

@Composable
fun SetupScreen(onStart: (Int, List<Int>) -> Unit) { /* وہی پرانا سیٹ اپ کوڈ */ }
