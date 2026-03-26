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
    val userArmy by engine.userArmyCount.collectAsState()
    val enemyArmy by engine.enemyArmyCount.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // دشمن کی چوکی اور لہر
        Text("ENEMY ARMIES: $enemyArmy", color = Color.Red, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))
        WaveView(wave = defenderWave, label = "ENEMY WAVE", color = Color.Red, isReversed = true)

        Spacer(modifier = Modifier.weight(1f))

        // آپ کی چوکی اور لہر
        WaveView(wave = attackerWave, label = "YOUR WAVE", color = Color.Cyan, isReversed = false)
        Spacer(modifier = Modifier.height(10.dp))
        Text("YOUR ARMIES: $userArmy", color = Color.Cyan, fontSize = 20.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(30.dp))

        // کنٹرول بٹنز
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            GameButton("R", Color.Gray) { engine.addUnitToWave(true, UnitType.ROCK) }
            GameButton("P", Color.White) { engine.addUnitToWave(true, UnitType.PAPER) }
            GameButton("S", Color.LightGray) { engine.addUnitToWave(true, UnitType.SCISSORS) }
        }
    }
}

@Composable
fun WaveView(wave: List<UnitType>, label: String, color: Color, isReversed: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = color.copy(alpha = 0.6f), fontSize = 10.sp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
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
            modifier = Modifier.padding(2.dp),
            colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, color)
        ) {
            Text(
                text = unit.name.take(1),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}

@Composable
fun GameButton(label: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.size(75.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333))
    ) {
        Text(label, color = color, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    }
}
