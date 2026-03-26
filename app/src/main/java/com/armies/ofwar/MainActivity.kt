package com.armies.ofwar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF121212)) {
                    BattleScreen(battleEngine)
                }
            }
        }
    }
}

@Composable
fun BattleScreen(engine: BattleEngine) {
    val attackerWave by engine.attackerWave.collectAsState()
    val defenderWave by engine.defenderWave.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("BATTLE FIELD", color = Color.Yellow, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        
        Spacer(modifier = Modifier.height(30.dp))

        // دشمن کی لہر (Defender)
        WaveDisplay(wave = defenderWave, label = "ENEMY WAVE", color = Color.Red)

        Spacer(modifier = Modifier.weight(1f))

        // آپ کی لہر (Attacker)
        WaveDisplay(wave = attackerWave, label = "YOUR WAVE", color = Color.Cyan)

        Spacer(modifier = Modifier.height(20.dp))

        // R, P, S کنٹرول بٹنز
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            RPSButton("R", Color.Gray) { engine.addUnitToWave(true, UnitType.ROCK) }
            RPSButton("P", Color.White) { engine.addUnitToWave(true, UnitType.PAPER) }
            RPSButton("S", Color.LightGray) { engine.addUnitToWave(true, UnitType.SCISSORS) }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun WaveDisplay(wave: List<UnitType>, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color.Gray, fontSize = 12.sp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(Color(0xFF1E1E1E), MaterialTheme.shapes.medium)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            wave.forEach { unit ->
                Text(
                    text = unit.name.take(1),
                    color = color,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp),
                    fontSize = 18.sp
                )
            }
        }
    }
}

@Composable
fun RPSButton(label: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.size(70.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333))
    ) {
        Text(label, color = color, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}
