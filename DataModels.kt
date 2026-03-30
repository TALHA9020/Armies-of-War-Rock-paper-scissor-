package com.armies.ofwar

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
    var totalReinforcements: Int = 5
)

data class Territory(
    val id: Int,
    val name: String,
    val ownerId: Int,
    val troops: Int,
    val neighbors: List<Int>, // کن علاقوں سے راستہ جڑا ہے
    val xPos: Float, // نقشے پر پوزیشن (فیصد میں)
    val yPos: Float
)

data class BattleResult(
    val attackerWon: Boolean,
    val message: String,
    val attackerRemaining: Int,
    val defenderRemaining: Int
)
