package com.armies.ofwar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF1A1A1A)) {
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
fun SetupScreen(onStart: (Int, List<Int>) -> Unit) {
    var armyCount by remember { mutableStateOf(2f) }
    val allies = remember { mutableStateListOf<Int>() }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("ARMIES OF WAR - SETUP", fontSize = 24.sp, color = Color.White)
        Spacer(Modifier.height(30.dp))
        
        Text("Total Armies: ${armyCount.toInt()}", color = Color.Cyan)
        Slider(value = armyCount, valueRange = 2f..10f, steps = 8, onValueChange = { armyCount = it })

        Text("Select Your Allies (Alliance):", color = Color.Gray, modifier = Modifier.padding(top = 20.dp))
        LazyColumn(Modifier.weight(1f)) {
            items((1 until armyCount.toInt()).toList()) { id ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = allies.contains(id), onCheckedChange = { 
                        if (it) allies.add(id) else allies.remove(id) 
                    })
                    Text("Army ${id + 1}", color = Color.White)
                }
            }
        }

        Button(onClick = { onStart(armyCount.toInt(), allies.toList()) }, modifier = Modifier.fillMaxWidth().padding(10.dp)) {
            Text("START WAR")
        }
    }
}

@Composable
fun BattleField(engine: BattleEngine) {
    val armies by engine.armies.collectAsState()
    val turnId by engine.currentTurnId.collectAsState()
    val currentArmy = armies.getOrNull(turnId)

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("CURRENT TURN: ${currentArmy?.name ?: ""}", color = currentArmy?.color ?: Color.White, fontSize = 20.sp)
        Spacer(Modifier.height(20.dp))
        
        // یہاں ہم لہروں اور ایکشن بٹن کا ڈیزائن شامل کر سکتے ہیں
        Text("Battle in Progress...", color = Color.Gray)
    }
}
