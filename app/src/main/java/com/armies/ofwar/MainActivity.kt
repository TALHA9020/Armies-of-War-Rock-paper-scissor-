package com.armies.ofwar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF121212)) {
                    if (!gameStarted) {
                        Box(contentAlignment = Alignment.Center) {
                            Button(onClick = { 
                                battleEngine.setupGame(5) 
                                gameStarted = true 
                            }) { Text("Start Armies of War") }
                        } [cite: 38, 40]
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
    [cite_start]val armies by engine.armies.collectAsState()
    val phase by engine.currentPhase.collectAsState()
    val cards by engine.userCards.collectAsState()
    val turnId by engine.currentTurnId.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        // Phase and Cards Card
        Card(modifier = Modifier.fillMaxWidth().padding(8.dp), colors = CardDefaults.cardColors(containerColor = Color.DarkGray)) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("CURRENT PHASE: ${phase.name}", color = if(phase == TurnPhase.ATTACK) Color.Red else Color.Cyan, fontSize = 20.sp)
                Text("Your Cards: $cards / 4", color = Color.Yellow, fontSize = 16.sp)
                if (cards >= 4) {
                    Button(onClick = { engine.exchangeCards() }, colors = ButtonDefaults.buttonColors(containerColor = Color.Green)) {
                        Text("Exchange 4 Cards for 20 Units", color = Color.Black)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text("WORLD MAP", color = Color.White, fontSize = 18.sp)

        // یہی وہ حصہ ہے جو نقشہ دکھائے گا
        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
            items(armies) { army ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF252525))) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(15.dp).background(army.color))
                        Spacer(Modifier.width(10.dp))
                        Text(army.name, color = Color.White, modifier = Modifier.weight(1f))
                        Text("${army.armyCount}", color = Color.Green, fontSize = 18.sp)
                    }
                }
            }
        }

        // Controls
        if (turnId == 0) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { engine.setPhase(TurnPhase.ATTACK) }, colors = ButtonDefaults.buttonColors(containerColor = if(phase == TurnPhase.ATTACK) Color.Red else Color.Gray)) { Text("ATTACK") }
                Button(onClick = { engine.setPhase(TurnPhase.MOVE) }, colors = ButtonDefaults.buttonColors(containerColor = if(phase == TurnPhase.MOVE) Color.Blue else Color.Gray)) { Text("MOVE") }
            }
            Row(Modifier.padding(8.dp)) {
                Button(onClick = { }) { Text("R") }
                Spacer(Modifier.width(5.dp))
                Button(onClick = { }) { Text("P") }
                Spacer(Modifier.width(5.dp))
                Button(onClick = { }) { Text("S") }
            }
            Button(onClick = { engine.endTurn() }, modifier = Modifier.fillMaxWidth()) { Text("FINISH TURN") }
        } else {
            CircularProgressIndicator(color = Color.Red)
            Text("Enemy Turn...", color = Color.White)
        }
    }
}
