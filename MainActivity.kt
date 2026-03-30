package com.armies.ofwar

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    private val engine = BattleEngine()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF1A1A1A)) {
                    RiskGameEngine()
                }
            }
        }
    }

    @Composable
    fun RiskGameEngine() {
        // --- Game State ---
        val players = remember { listOf(
            Player(1, "You", Color(0xFF2196F3)),
            Player(2, "AI Enemy", Color(0xFFF44336))
        ) }

        var territories by remember { mutableStateOf(listOf(
            Territory(0, "Base", 1, 10, listOf(1, 2), 0.2f, 0.2f),
            Territory(1, "Border North", 2, 5, listOf(0, 3), 0.5f, 0.2f),
            Territory(2, "Border South", 2, 4, listOf(0, 3), 0.5f, 0.5f),
            Territory(3, "Enemy Capital", 2, 15, listOf(1, 2), 0.8f, 0.4f)
        )) }

        var selectedFrom by remember { mutableStateOf<Territory?>(null) }
        var selectedTo by remember { mutableStateOf<Territory?>(null) }
        var attackerChoices = remember { mutableStateListOf<RPSChoice>() }
        var turnPhase by remember { mutableStateOf("ATTACK") } // REINFORCE, ATTACK, END

        // --- UI Layout ---
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Armies of War", color = Color.White, fontSize = 24.sp)
                Badge { Text("Phase: $turnPhase") }
            }

            // Map Area (Interactive)
            Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color(0xFF252525))) {
                territories.forEach { territory ->
                    TerritoryNode(
                        territory = territory,
                        isSelected = selectedFrom?.id == territory.id || selectedTo?.id == territory.id,
                        onCLick = {
                            if (selectedFrom == null && territory.ownerId == 1) {
                                selectedFrom = territory
                            } else if (selectedFrom != null && territory.id != selectedFrom!!.id) {
                                if (selectedFrom!!.neighbors.contains(territory.id)) {
                                    selectedTo = territory
                                } else {
                                    selectedFrom = null
                                    selectedTo = null
                                }
                            }
                        }
                    )
                }
            }

            // Controls Area
            Card(modifier = Modifier.fillMaxWidth().height(250.dp), shape = MaterialTheme.shapes.large) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    if (selectedFrom != null && selectedTo != null) {
                        Text("حملہ: ${selectedFrom!!.name} ➔ ${selectedTo!!.name}", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row {
                            RPSButton("🪨") { if(attackerChoices.size < 3) attackerChoices.add(RPSChoice.ROCK) }
                            RPSButton("📄") { if(attackerChoices.size < 3) attackerChoices.add(RPSChoice.PAPER) }
                            RPSButton("✂️") { if(attackerChoices.size < 3) attackerChoices.add(RPSChoice.SCISSORS) }
                        }

                        if (attackerChoices.size == 3) {
                            Button(onClick = {
                                val aiChoices = List(3) { RPSChoice.values().random() }
                                val result = engine.resolveBattle(attackerChoices, aiChoices, selectedFrom!!.troops, selectedTo!!.troops)
                                
                                // Update Map
                                territories = territories.map {
                                    when (it.id) {
                                        selectedFrom!!.id -> it.copy(troops = result.attackerRemaining)
                                        selectedTo!!.id -> if(result.attackerWon) it.copy(ownerId = 1, troops = 5) else it.copy(troops = result.defenderRemaining)
                                        else -> it
                                    }
                                }
                                Toast.makeText(this@MainActivity, result.message, Toast.LENGTH_SHORT).show()
                                attackerChoices.clear()
                                selectedFrom = null
                                selectedTo = null
                            }) {
                                Text("جنگ شروع کریں!")
                            }
                        }
                    } else {
                        Text("اپنا علاقہ منتخب کریں اور پھر دشمن کے جڑے ہوئے علاقے پر کلک کریں")
                    }
                }
            }
        }
    }

    @Composable
    fun TerritoryNode(territory: Territory, isSelected: Boolean, onCLick: () -> Unit) {
        Box(modifier = Modifier
            .offset(x = (territory.xPos * 300).dp, y = (territory.yPos * 500).dp)
            .size(80.dp)
            .background(if (territory.ownerId == 1) Color(0xFF2196F3) else Color(0xFFF44336), CircleShape)
            .border(if (isSelected) 4.dp else 0.dp, Color.White, CircleShape)
            .clickable { onCLick() },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(territory.name, color = Color.White, fontSize = 10.sp)
                Text("${territory.troops}", color = Color.White, fontSize = 18.sp)
            }
        }
    }

    @Composable
    fun RPSButton(label: String, onClick: () -> Unit) {
        OutlinedButton(onClick = onClick, modifier = Modifier.padding(4.dp)) { Text(label, fontSize = 20.sp) }
    }
}
