package com.armies.ofwar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
            MaterialTheme {
                // گیم شروع ہوتے ہی کمپیوٹر (AI) کا اٹیک شروع کرنے کے لیے
                LaunchedEffect(Unit) {
                    battleEngine.startAIEnemy()
                }

                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF1A1A1A)) {
                    BattleField(battleEngine)
                }
            }
        }
    }
}

@Composable
fun BattleField(engine: BattleEngine) {
    val attackerWave by engine.attackerWave.collectAsState()
    val defenderWave by engine.defenderWave.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("ARMIES OF WAR", color = Color(0xFFFFD700), fontSize = 28.sp, fontWeight = FontWeight.Bold)
        
        Spacer(modifier = Modifier.height(40.dp))

        // ڈیفنڈر کی لہر (اوپر) - اسے سرخ رنگ دیا گیا ہے
        WaveView(wave = defenderWave, label = "DEFENDER (ADVANTAGE)", color = Color.Red, isReversed = true)

        Spacer(modifier = Modifier.weight(1f))

        // اٹیکر کی لہر (نیچے) - اسے نیلا (Cyan) رنگ دیا گیا ہے
        WaveView(wave = attackerWave, label = "ATTACKER", color = Color.Cyan, isReversed = false)

        Spacer(modifier = Modifier.height(30.dp))

        // کنٹرول بٹنز (R, P, S)
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            GameButton("R", Color.Gray) { engine.addUnitToWave(true, UnitType.ROCK) }
            GameButton("P", Color.White) { engine.addUnitToWave(true, UnitType.PAPER) }
            GameButton("S", Color.LightGray) { engine.addUnitToWave(true, UnitType.SCISSORS) }
        }
        
        // ڈیفنڈر کے بٹنز (صرف مینوئل ٹیسٹنگ کے لیے)
        Text("DEBUG: DEFENDER CONTROLS", color = Color.DarkGray, fontSize = 10.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = { engine.addUnitToWave(false, UnitType.ROCK) }) { Text("R") }
            Button(onClick = { engine.addUnitToWave(false, UnitType.PAPER) }) { Text("P") }
            Button(onClick = { engine.addUnitToWave(false, UnitType.SCISSORS) }) { Text("S") }
        }
    }
}

@Composable
fun WaveView(wave: List<UnitType>, label: String, color: Color, isReversed: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = color.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(Color(0xFF252525), RoundedCornerShape(12.dp))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            LazyRow(
                reverseLayout = isReversed,
                horizontalArrangement = if (isReversed) Arrangement.End else Arrangement.Start,
                modifier = Modifier.fillMaxWidth()
            ) {
                items(wave) { unit ->
                    AnimatedUnit(unit, color)
                }
            }
        }
    }
}

@Composable
fun AnimatedUnit(unit: UnitType, color: Color) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)) + expandHorizontally(),
        exit = fadeOut()
    ) {
        Card(
            modifier = Modifier.padding(4.dp),
            colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.2f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, color)
        ) {
            Text(
                text = unit.name.take(1),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            ) [cite: 33]
        }
    }
}

@Composable
fun GameButton(label: String, color: Color, onClick: () -> Unit) {
    FilledTonalButton(
        onClick = onClick,
        modifier = Modifier.size(80.dp),
        shape = CircleShape,
        [cite_start]colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFF333333))
    ) {
        Text(label, color = color, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
    }
}
