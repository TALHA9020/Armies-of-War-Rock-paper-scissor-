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
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF121212) // ڈارک تھیم
                ) {
                    if (!gameStarted) {
                        // گیم شروع کرنے کا بٹن
                        Box(contentAlignment = Alignment.Center) {
                            Button(onClick = { 
                                battleEngine.setupGame(5) // 5 فوجوں کے ساتھ شروع کریں
                                gameStarted = true 
                            }) {
                                Text("Start Armies of War")
                            }
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // اوپر کی معلومات (Phase اور Cards)
        Card(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.DarkGray)
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("CURRENT PHASE: ${phase.name}", color = if(phase == TurnPhase.ATTACK) Color.Red else Color.Cyan, fontSize = 20.sp)
                Text("Your Cards: $cards / 4", color = Color.Yellow, fontSize = 16.sp)
                
                if (cards >= 4) {
                    Button(
                        onClick = { engine.exchangeCards() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
                    ) {
                        Text("Exchange 4 Cards for 20 Units", color = Color.Black)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // باری کس کی ہے؟
        Text(
            text = if (turnId == 0) "YOUR TURN" else "ENEMY TURN",
            color = if (turnId == 0) Color.Green else Color.Red,
            fontSize = 24.sp
        )

        Spacer(modifier = Modifier.weight(1f))

        // فیز کنٹرول بٹن (صرف یوزر کی باری پر نظر آئیں گے)
        if (turnId == 0) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(
                    onClick = { engine.setPhase(TurnPhase.ATTACK) },
                    colors = ButtonDefaults.buttonColors(containerColor = if(phase == TurnPhase.ATTACK) Color.Red else Color.Gray)
                ) { Text("ATTACK MODE") }

                Button(
                    onClick = { engine.setPhase(TurnPhase.MOVE) },
                    colors = ButtonDefaults.buttonColors(containerColor = if(phase == TurnPhase.MOVE) Color.Blue else Color.Gray)
                ) { Text("MOVE MODE") }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // یونٹ بھیجنے کے بٹن
            Row {
                Button(onClick = { /* یہاں انجن کا اٹیک فنکشن کال ہوگا */ }) { Text("R") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { /* یہاں انجن کا اٹیک فنکشن کال ہوگا */ }) { Text("P") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { /* یہاں انجن کا اٹیک فنکشن کال ہوگا */ }) { Text("S") }
            }

            Button(
                onClick = { engine.endTurn() },
                modifier = Modifier.padding(top = 16.dp).fillMaxWidth()
            ) {
                Text("FINISH TURN")
            }
        } else {
            // دشمن کی باری کے دوران لوڈنگ میسج
            CircularProgressIndicator(color = Color.Red)
            Text("Enemy is attacking...", color = Color.White)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // آپ کی یونٹس کی کل تعداد
        Text("Your Total Units: ${armies.firstOrNull { it.id == 0 }?.armyCount ?: 0}", color = Color.White)
    }
}
