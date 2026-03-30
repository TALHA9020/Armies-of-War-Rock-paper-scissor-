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
import androidx.compose.ui.text.font.FontWeight
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
        var gameLog by remember { mutableStateOf("آپ کی باری: حملہ کرنے کے لیے علاقہ منتخب کریں") }

        // AI Logic
        LaunchedEffect(currentPlayer) {
            if (currentPlayer == 2) {
                gameLog = "کمپیوٹر (AI) باری لے رہا ہے..."
                delay(1500)
                val aiSource = territories.filter { it.ownerId == 2 && it.troops > 2 }.randomOrNull()
                val target = aiSource?.neighbors?.map { id -> territories.find { t -> t.id == id }!! }?.find { it.ownerId == 1 }

                if (aiSource != null && target != null) {
                    val res = engine.resolve3vs3Clash(List(3){RPSChoice.random()}, List(3){RPSChoice.random()}, aiSource.troops, target.troops)
                    territories = updateMap(territories, aiSource.id, target.id, res, 2)
                    gameLog = "AI نے حملہ کیا: ${res.message}"
                }
                delay(1000)
                currentPlayer = 1
                gameLog = "آپ کی باری: نئی فوجیں شامل کر دی گئیں۔"
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Column {
                // Info Section
                Card(modifier = Modifier.fillMaxWidth().padding(8.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(gameLog, color = Color.White, modifier = Modifier.weight(1f), fontSize = 14.sp)
                        if(currentPlayer == 1) {
                            Button(onClick = { currentPlayer = 2 }) { Text("باری ختم") }
                        }
                    }
                }

                // Map Section
                BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    val w = maxWidth; val h = maxHeight
                    territories.forEach { t ->
                        TerritoryNode(t, isSelected = (selectedFrom?.id == t.id || selectedTo?.id == t.id), w, h) {
                            if (currentPlayer == 1) {
                                if (selectedFrom == null && t.ownerId == 1) selectedFrom = t
                                else if (selectedFrom != null && t.neighbors.contains(selectedFrom!!.id) && t.ownerId == 2) {
                                    selectedTo = t
                                    showBattleOverlay = true
                                } else { selectedFrom = null; selectedTo = null }
                            }
                        }
                    }
                }
            }

            // Battle Overlay
            if (showBattleOverlay) {
                BattleOverlay(attackerChoices, onComplete = {
                    val res = engine.resolve3vs3Clash(attackerChoices.toList(), List(3){RPSChoice.random()}, selectedFrom!!.troops, selectedTo!!.troops)
                    territories = updateMap(territories, selectedFrom!!.id, selectedTo!!.id, res, 1)
                    gameLog = res.message
                    attackerChoices.clear(); selectedFrom = null; selectedTo = null; showBattleOverlay = false
                })
            }
        }
    }

    @Composable
    fun BattleOverlay(choices: MutableList<RPSChoice>, onComplete: () -> Unit) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.85f)), contentAlignment = Alignment.Center) {
            Card(modifier = Modifier.padding(24.dp), shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("3vs3 RPS Clash", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row {
                        repeat(3) { i ->
                            val c = if(i < choices.size) choices[i] else RPSChoice.NONE
                            Box(modifier = Modifier.padding(4.dp).size(60.dp).background(Color.Gray, CircleShape), contentAlignment = Alignment.Center) {
                                Text(getEmoji(c), fontSize = 24.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    if (choices.size < 3) {
                        Row {
                            RPSBtn("🪨") { choices.add(RPSChoice.ROCK) }
                            RPSBtn("📄") { choices.add(RPSChoice.PAPER) }
                            RPSBtn("✂️") { choices.add(RPSChoice.SCISSORS) }
                        }
                    } else {
                        Button(onClick = onComplete) { Text("جنگ شروع کریں!") }
                    }
                }
            }
        }
    }

    @Composable
    fun TerritoryNode(t: Territory, isSelected: Boolean, w: androidx.compose.ui.unit.Dp, h: androidx.compose.ui.unit.Dp, onClick: () -> Unit) {
        Box(modifier = Modifier
            .offset(x = w * t.xPos, y = h * t.yPos)
            .size(70.dp)
            .background(t.color, CircleShape)
            .border(if (isSelected) 4.dp else 0.dp, Color.Yellow, CircleShape)
            .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Text("${t.troops}", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
        }
    }

    @Composable
    fun RPSBtn(txt: String, onClick: () -> Unit) {
        Button(onClick = onClick, modifier = Modifier.padding(4.dp)) { Text(txt, fontSize = 24.sp) }
    }

    private fun updateMap(list: List<Territory>, fId: Int, tId: Int, res: BattleResult, attId: Int): List<Territory> {
        return list.map {
            when (it.id) {
                fId -> it.copy(troops = res.attackerRemaining)
                tId -> if (res.attackerWon) it.copy(ownerId = attId, troops = 3) else it.copy(troops = res.defenderRemaining)
                else -> it
            }
        }
    }

    private fun initialMap() = listOf(
        Territory(0, "Home", 1, 15, listOf(1, 2), 0.15f, 0.2f),
        Territory(1, "North", 2, 8, listOf(0, 3), 0.5f, 0.15f),
        Territory(2, "South", 2, 7, listOf(0, 3), 0.45f, 0.5f),
        Territory(3, "Enemy HQ", 2, 20, listOf(1, 2), 0.8f, 0.3f)
    )

    private fun getEmoji(c: RPSChoice) = when(c) {
        RPSChoice.ROCK -> "🪨"; RPSChoice.PAPER -> "📄"; RPSChoice.SCISSORS -> "✂️"; else -> "?"
    }
}
