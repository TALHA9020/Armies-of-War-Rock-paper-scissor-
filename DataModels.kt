package com.armies.ofwar

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color

enum class RPSChoice { ROCK, PAPER, SCISSORS;
    fun beats(other: RPSChoice): Boolean = when {
        this == ROCK && other == SCISSORS -> true
        this == PAPER && other == ROCK -> true
        this == SCISSORS && other == PAPER -> true
        else -> false
    }
}

data class Player(
    val id: Int,
    val name: String,
    val color: Color,
    val isHuman: Boolean = false
)

data class Territory(
    val id: Int,
    val name: String,
    val ownerId: Int,
    val troops: Int,
    val neighbors: List<Int> // ان علاقوں کے IDs جن پر یہاں سے حملہ ہو سکتا ہے
)

// گیم کی موجودہ صورتحال کو سنبھالنے کے لیے
data class GameState(
    val players: List<Player>,
    val territories: List<Territory>,
    val currentPlayerIndex: Int = 0,
    val statusMessage: String = "آپ کی باری ہے!"
)
