package com.armies.ofwar

import androidx.compose.ui.graphics.Color

enum class RPSChoice { 
    ROCK, PAPER, SCISSORS, NONE;
    companion object { fun random() = values().filter { it != NONE }.random() }
    fun beats(other: RPSChoice): Boolean = (this == ROCK && other == SCISSORS) || 
        (this == PAPER && other == ROCK) || (this == SCISSORS && other == PAPER)
}

data class Territory(
    val id: Int,
    val name: String,
    val ownerId: Int, // 1: Player, 2: AI
    val troops: Int,
    val neighbors: List<Int>,
    val xPos: Float,
    val yPos: Float
)

data class BattleResult(
    val attackerWon: Boolean,
    val attackerPoints: Int,
    val defenderPoints: Int,
    val attackerRemaining: Int,
    val defenderRemaining: Int
)
