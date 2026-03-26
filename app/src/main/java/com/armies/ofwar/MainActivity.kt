package com.armies.ofwar

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF121212)) {
                    GameMainScreen()
                }
            }
        }
    }
}

@Composable
fun GameMainScreen() {
    val context = LocalContext.current
    // عارضی ڈیٹا (اگلے سٹیپ میں ہم اسے GameEngine سے جوڑیں گے)
    var userArmies by remember { mutableIntStateOf(20) }
    var enemyArmies by remember { mutableIntStateOf(20) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "ARMIES OF WAR",
            style = MaterialTheme.typography.headlineMedium,
            color = Color(0xFFFFD700),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // سٹیٹس کارڈ (پرانے پری ویو باکس کی طرح)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(20.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("YOUR ARMIES", color = Color.Gray, fontSize = 12.sp)
                    Text("$userArmies", color = Color.Cyan, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                }
                Text("VS", color = Color.Red, fontSize = 24.sp, modifier = Modifier.align(Alignment.CenterVertically))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("ENEMY ARMIES", color = Color.Gray, fontSize = 12.sp)
                    Text("$enemyArmies", color = Color.Red, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ایکشن بٹن (حملہ کرنے کے لیے)
        Button(
            onClick = {
                if (userArmies > 1) {
                    userArmies -= 2
                    enemyArmies -= 3
                    Toast.makeText(context, "Attacking Enemy Territory!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Not enough troops!", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth().height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("LAUNCH ATTACK", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ری سیٹ بٹن (جیسے آپ کے پرانے کوڈ میں تھا)
        OutlinedButton(
            onClick = {
                userArmies = 20
                enemyArmies = 20
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
        ) {
            Text("Reset War")
        }
    }
}
