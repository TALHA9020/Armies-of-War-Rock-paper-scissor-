package com.armies.ofwar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.armies.ofwar.ui.theme.ArmiesOfWarTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { 
            ArmiesOfWarTheme { 
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) { 
                    GameSetupScreen() // This will be your initial screen
                }
            }
        }
    }
}

@Composable
fun GameSetupScreen() {
    // Implementation for Game Setup
}

@Composable
fun RiskGameScreen() {
    // Implementation for Risk Game
}

@Composable
fun GameEndScreen() {
    // Implementation for Game End Screen
}

// Additional Composables like RPSButtonCompact can go here.

// Localization support for Urdu can be managed via strings.xml
// Example of Urdu localization strings: 
// <string name="game_title">اس کھیل کا عنوان</string
