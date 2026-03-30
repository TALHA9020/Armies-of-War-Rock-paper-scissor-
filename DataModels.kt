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
    val ownerId: Int, // 1: Player (Blue), 2: AI (Red)
    val troops: Int,
    val neighbors: List<Int>,
    val xPos: Float,
    val yPos: Float,
    val color: Color = if (ownerId == 1) Color(0xFF1976D2) else Color(0xFFD32F2F)
)

data class BattleResult(
    val attackerWon: Boolean,
    val attackerRemaining: Int,
    val defenderRemaining: Int,
    val message: String
)
