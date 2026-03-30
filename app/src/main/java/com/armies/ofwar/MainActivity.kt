package com.armies.ofwar

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
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
                GameScreen()
            }
        }
    }

    @Composable
    fun GameScreen() {
        var attackerChoices by remember { mutableStateOf(mutableListOf<RPSChoice>()) }
        var battleLog by remember { mutableStateOf("جنگ شروع کرنے کے لیے 3 انتخاب کریں!") }
        
        // Dummy Data for Demo
        val attackerTroops = 15
        val defenderTroops = 10

        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Armies of War", style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(20.dp))
            
            Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("حملہ آور فوج: $attackerTroops")
                    Text("دفاعی فوج: $defenderTroops")
                    Text("انتخابات: ${attackerChoices.joinToString(", ")}")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (attackerChoices.size < 3) {
                Row {
                    RPSButton("Rock") { attackerChoices.add(RPSChoice.ROCK); checkBattle(attackerChoices, attackerTroops, defenderTroops) }
                    RPSButton("Paper") { attackerChoices.add(RPSChoice.PAPER); checkBattle(attackerChoices, attackerTroops, defenderTroops) }
                    RPSButton("Scissors") { attackerChoices.add(RPSChoice.SCISSORS); checkBattle(attackerChoices, attackerTroops, defenderTroops) }
                }
            } else {
                Button(onClick = { attackerChoices = mutableListOf() }) {
                    Text("دوبارہ کھیلیں")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(battleLog, color = Color.DarkGray)
        }
    }

    @Composable
    fun RPSButton(label: String, onClick: () -> Unit) {
        Button(onClick = onClick, modifier = Modifier.padding(4.dp)) {
            Text(label)
        }
    }

    private fun checkBattle(choices: MutableList<RPSChoice>, aT: Int, dT: Int) {
        if (choices.size == 3) {
            // Simulate AI Defender
            val defenderChoices = List(3) { RPSChoice.values().random() }
            val result = engine.resolveBattle(aT, dT, choices, defenderChoices)
            
            Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
        }
    }
}
