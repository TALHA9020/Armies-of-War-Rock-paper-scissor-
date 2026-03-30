package com.armies.ofwar

import androidx.compose.ui.graphics.Color
import java.io.Serializable

enum class RPSChoice { ROCK, PAPER, SCISSORS;
    fun beats(other: RPSChoice): Boolean = when {
        this == ROCK && other == SCISSORS -> true
        this == PAPER && other == ROCK -> true
        this == SCISSORS && other == PAPER -> true
        else -> false
    }
}

data class Territory(
    val id: Int,
    val name: String,
    val ownerId: Int,
    var troops: Int,
    val color: Color,
    val neighbors: List<Int>
)

data class Player(
    val id: Int,
    val name: String,
    val color: Color,
    var totalTroops: Int,
    val isHuman: Boolean = true
)

data class BattleState(
    val attackerId: Int,
    val defenderId: Int,
    val attackerTerritoryId: Int,
    val defenderTerritoryId: Int,
    var attackerChoices: MutableList<RPSChoice> = mutableListOf(),
    var defenderChoices: MutableList<RPSChoice> = mutableListOf(),
    var currentRound: Int = 1
)
