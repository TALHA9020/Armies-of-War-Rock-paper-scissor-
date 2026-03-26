package com.armies.ofwar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
        Text("ARMIES OF WAR - SETUP", fontSize = 24.sp, color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(30.dp))
        
        Text("Total Armies: ${armyCount.toInt()}", color = Color.Cyan)
        Slider(value = armyCount, valueRange = 2f..10f, steps = 7, onValueChange = { armyCount = it })

        Text("Select Your Allies:", color = Color.Gray, modifier = Modifier.padding(top = 20.dp))
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

        Button(onClick = { onStart(armyCount.toInt(), allies.toList()) }, modifier = Modifier.fillMaxWidth()) {
            Text("START WAR")
        }
    }
}

@Composable
fun BattleField(engine: BattleEngine) {
    val armies by engine.armies.collectAsState()
    val turnId by engine.currentTurnId.collectAsState()
    val attackerWave by engine.attackerWave.collectAsState()
    val defenderWave by engine.defenderWave.collectAsState()

    val currentArmy = armies.getOrNull(turnId)

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        // دشمن کی معلومات (صرف وہ جو آپ کے الائنس میں نہیں ہے)
        Text("BATTLEFIELD", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        
        Spacer(Modifier.height(20.dp))

        // دفاعی لہر (Defender Wave)
        WaveBox(wave = defenderWave, label = "ENEMY FORCE", color = Color.Red)

        Spacer(modifier = Modifier.weight(1f))

        // حملہ آور لہر (Attacker Wave)
        WaveBox(wave = attackerWave, label = "YOUR FORCE", color = Color.Cyan)

        Spacer(modifier = Modifier.height(20.dp))

        // موجودہ باری اور کنٹرولز
        if (currentArmy?.isUserControlled == true) {
            Text("YOUR TURN", color = Color.Cyan, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                UnitButton("R") { engine.addUnitToWave(true, UnitType.ROCK) }
                UnitButton("P") { engine.addUnitToWave(true, UnitType.PAPER) }
                UnitButton("S") { engine.addUnitToWave(true, UnitType.SCISSORS) }
            }
        } else {
            Text("${currentArmy?.name ?: "ENEMY"}'S TURN", color = currentArmy?.color ?: Color.Red)
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        
        // فوج کی کل تعداد دکھانا
        Text("Your Units: ${armies.firstOrNull()?.armyCount ?: 0}", color = Color.Cyan)
    }
}

@Composable
fun WaveBox(wave: List<UnitType>, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 12.sp, color = color.copy(alpha = 0.7f))
        Box(modifier = Modifier.fillMaxWidth().height(80.dp).background(Color(0xFF2A2A2A), RoundedCornerShape(8.dp)).padding(8.dp)) {
            LazyRow {
                items(wave) { unit ->
                    Text(unit.name.take(1), modifier = Modifier.padding(4.dp).background(color, CircleShape).padding(8.dp), color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun UnitButton(label: String, onClick: () -> Unit) {
    Button(onClick = onClick, shape = CircleShape, modifier = Modifier.size(60.dp), contentPadding = PaddingValues(0.dp)) {
        Text(label, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}
