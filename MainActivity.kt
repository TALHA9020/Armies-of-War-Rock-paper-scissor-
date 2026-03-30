package com.armies.ofwar

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    private val engine = BattleEngine()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                RiskGameScreen()
            }
        }
    }

    @Composable
    fun RiskGameScreen() {
        // Sample Players
        val p1 = Player(1, "Player 1 (You)", Color(0xFF2196F3), true)
        val p2 = Player(2, "AI Enemy", Color(0xFFF44336), false)

        // Initial Map (4 Territories)
        var territories by remember { mutableStateOf(listOf(
            Territory(0, "Base Alpha", 1, 10, listOf(1, 2)),
            Territory(1, "Frontier A", 2, 5, listOf(0, 3)),
            Territory(2, "Frontier B", 2, 5, listOf(0, 3)),
            Territory(3, "Enemy Core", 2, 12, listOf(1, 2))
        )) }

        var selectedTerritory by remember { mutableStateOf<Territory?>(null) }
        var attackerChoices = remember { mutableStateListOf<RPSChoice>() }

        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text("Armies of War: Strategy", style = MaterialTheme.typography.headlineMedium)
            
            Spacer(modifier = Modifier.height(16.dp))

            // Map Representation
            LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.weight(1f)) {
                items(territories) { territory ->
                    val owner = if (territory.ownerId == 1) p1 else p2
                    Card(
                        modifier = Modifier
                            .padding(8.dp)
                            .height(100.dp)
                            .clickable { selectedTerritory = territory },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedTerritory?.id == territory.id) Color.Yellow else owner.color
                        )
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text("${territory.name}\nTroops: ${territory.troops}", color = Color.White)
                        }
                    }
                }
            }

            // Battle Controls
            if (selectedTerritory != null && selectedTerritory?.ownerId == 2) {
                Card(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("حملہ کریں: ${selectedTerritory?.name}")
                        Text("اپنے 3 داؤ منتخب کریں: ${attackerChoices.size}/3")
                        
                        Row {
                            RPSButton("🪨") { if(attackerChoices.size < 3) attackerChoices.add(RPSChoice.ROCK) }
                            RPSButton("📄") { if(attackerChoices.size < 3) attackerChoices.add(RPSChoice.PAPER) }
                            RPSButton("✂️") { if(attackerChoices.size < 3) attackerChoices.add(RPSChoice.SCISSORS) }
                        }

                        if (attackerChoices.size == 3) {
                            Button(onClick = {
                                val aiChoices = List(3) { RPSChoice.values().random() }
                                val res = engine.resolveBattle(attackerChoices, aiChoices, 10, selectedTerritory!!.troops)
                                
                                Toast.makeText(this@MainActivity, res.message, Toast.LENGTH_SHORT).show()
                                
                                // Map Update Logic
                                if (res.attackerWon) {
                                    territories = territories.map { 
                                        if (it.id == selectedTerritory!!.id) it.copy(ownerId = 1, troops = res.remainingAttackerTroops) 
                                        else it 
                                    }
                                }
                                attackerChoices.clear()
                                selectedTerritory = null
                            }) {
                                Text("جنگ شروع کریں!")
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun RPSButton(label: String, onClick: () -> Unit) {
        Button(onClick = onClick, modifier = Modifier.padding(4.dp)) { Text(label) }
    }
}
