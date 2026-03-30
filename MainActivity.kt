package com.armies.ofwar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private val engine = BattleEngine()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF121212)) {
                    FullRiskGame()
                }
            }
        }
    }

    @Composable
    fun FullRiskGame() {
        var territories by remember { mutableStateOf(initialMap()) }
        var currentPlayer by remember { mutableIntStateOf(1) }
        var selectedFrom by remember { mutableStateOf<Territory?>(null) }
        var selectedTo by remember { mutableStateOf<Territory?>(null) }
        val attackerChoices = remember { mutableStateListOf<RPSChoice>() }
        var showBattleOverlay by remember { mutableStateOf(false) }
        var gameStatus by remember { mutableStateOf("آپ کی باری: علاقہ منتخب کریں") }

        // AI Turn Logic
        LaunchedEffect(currentPlayer) {
            if (currentPlayer == 2) {
                gameStatus = "کمپیوٹر سوچ رہا ہے..."
                delay(2000)
                val aiSource = territories.filter { it.ownerId == 2 && it.troops > 3 }.randomOrNull()
                val target = aiSource?.neighbors?.map { id -> territories.find { it.id == id }!! }?.find { it.ownerId == 1 }

                if (aiSource != null && target != null) {
                    val aiMoves = List(3) { RPSChoice.random() }
                    val playerMoves = List(3) { RPSChoice.random() }
                    val res = engine.resolve3vs3Clash(aiMoves, playerMoves, aiSource.troops, target.troops)
                    territories = updateMap(territories, aiSource.id, target.id, res, 2)
                }
                // End AI Turn & Add Reinforcements
                territories = territories.map { if(it.ownerId == 2) it.copy(troops = it.troops + 1) else it }
                currentPlayer = 1
                gameStatus = "آپ کی باری: حملہ کریں!"
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Column {
                // Info Bar
                Card(modifier = Modifier.fillMaxWidth().padding(8.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(gameStatus, color = if(currentPlayer == 1) Color(0xFF2196F3) else Color.Red)
                        Spacer(modifier = Modifier.weight(1f))
                        if(currentPlayer == 1) {
                            Button(onClick = { 
                                territories = territories.map { if(it.ownerId == 1) it.copy(troops = it.troops + 2) else it }
                                currentPlayer = 2 
                            }) { Text("باری ختم") }
                        }
                    }
                }

                // Map Display
                Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color(0xFF151515))) {
                    territories.forEach { territory ->
                        TerritoryNode(territory, isSelected = (selectedFrom?.id == territory.id || selectedTo?.id == territory.id)) {
                            if (currentPlayer == 1) {
                                if (selectedFrom == null && territory.ownerId == 1) selectedFrom = territory
                                else if (selectedFrom != null && territory.neighbors.contains(selectedFrom!!.id)) {
                                    selectedTo = territory
                                    showBattleOverlay = true
                                } else { selectedFrom = null; selectedTo = null }
                            }
                        }
                    }
                }
            }

            // 3vs3 RPS Battle Overlay
            if (showBattleOverlay && selectedTo != null) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.8f)), contentAlignment = Alignment.Center) {
                    Card(modifier = Modifier.fillMaxWidth(0.9f).padding(16.dp)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                            Text("3vs3 RPS Clash", fontSize = 20.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                            Text("${selectedFrom?.name} vs ${selectedTo?.name}", fontSize = 12.sp)
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            // Slots UI
                            Row(horizontalArrangement = Arrangement.Center) {
                                repeat(3) { i ->
                                    val choice = if (i < attackerChoices.size) attackerChoices[i] else RPSChoice.NONE
                                    Box(modifier = Modifier.padding(4.dp).size(50.dp).background(Color.DarkGray, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                                        Text(if(choice == RPSChoice.NONE) "?" else getEmoji(choice))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            if (attackerChoices.size < 3) {
                                Row {
                                    RPSBtn("🪨") { attackerChoices.add(RPSChoice.ROCK) }
                                    RPSBtn("📄") { attackerChoices.add(RPSChoice.PAPER) }
                                    RPSBtn("✂️") { attackerChoices.add(RPSChoice.SCISSORS) }
                                }
                            } else {
                                Button(onClick = {
                                    val defChoices = List(3) { RPSChoice.random() }
                                    val res = engine.resolve3vs3Clash(attackerChoices.toList(), defChoices, selectedFrom!!.troops, selectedTo!!.troops)
                                    territories = updateMap(territories, selectedFrom!!.id, selectedTo!!.id, res, 1)
                                    
                                    attackerChoices.clear()
                                    selectedFrom = null; selectedTo = null
                                    showBattleOverlay = false
                                }) { Text("جنگ کا نتیجہ دیکھیں") }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun TerritoryNode(t: Territory, isSelected: Boolean, onClick: () -> Unit) {
        Box(modifier = Modifier
            .offset(x = (t.xPos * 300).dp, y = (t.yPos * 550).dp)
            .size(65.dp)
            .background(if (t.ownerId == 1) Color(0xFF1976D2) else Color(0xFFD32F2F), CircleShape)
            .border(if (isSelected) 3.dp else 0.dp, Color.White, CircleShape)
            .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Text("${t.troops}", color = Color.White, fontSize = 18.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        }
    }

    @Composable
    fun RPSBtn(lbl: String, onClick: () -> Unit) {
        Button(onClick = onClick, modifier = Modifier.padding(4.dp)) { Text(lbl, fontSize = 20.sp) }
    }

    private fun updateMap(list: List<Territory>, fromId: Int, toId: Int, res: BattleResult, attackerId: Int): List<Territory> {
        return list.map {
            when (it.id) {
                fromId -> it.copy(troops = res.attackerRemaining)
                toId -> if (res.attackerWon) it.copy(ownerId = attackerId, troops = 3) else it.copy(troops = res.defenderRemaining)
                else -> it
            }
        }
    }

    private fun initialMap() = listOf(
        Territory(0, "Home", 1, 12, listOf(1, 2), 0.1f, 0.2f),
        Territory(1, "North", 2, 6, listOf(0, 3), 0.5f, 0.1f),
        Territory(2, "South", 2, 5, listOf(0, 3), 0.5f, 0.4f),
        Territory(3, "Enemy", 2, 15, listOf(1, 2), 0.8f, 0.25f)
    )

    private fun getEmoji(c: RPSChoice) = when(c) {
        RPSChoice.ROCK -> "🪨"
        RPSChoice.PAPER -> "📄"
        RPSChoice.SCISSORS -> "✂️"
        else -> ""
    }
}
