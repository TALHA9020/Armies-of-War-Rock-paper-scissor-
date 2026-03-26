package com.armies.ofwar

import androidx.compose.ui.graphics.Color
import java.io.Serializable

enum class UnitType { ROCK, PAPER, SCISSORS, NONE }

enum class TerritoryLevel(val label: String) { 
    POST("پوسٹ"), CITY("شہر"), PROVINCE("صوبہ"), COUNTRY("ملک"), 
    CONTINENT("بر اعظم"), PLANET("سیارہ"), SOLAR_SYSTEM("نظام شمسی"), 
    GALAXY("کہکشاں"), UNIVERSE("کائنات") 
}

enum class CardType { ROCK, PAPER, SCISSORS }

data class UnitCounts(
    var rocks: Int = 0,
    var papers: Int = 0,
    var scissors: Int = 0
) : Serializable {
    val total: Int get() = rocks + papers + scissors
}

data class Outpost(
    val id: Int,
    var ownerId: Int,
    var units: UnitCounts,
    var posX: Float,
    var posY: Float,
    var level: TerritoryLevel = TerritoryLevel.POST
) : Serializable

data class Army(
    val id: Int,
    val name: String,
    val color: Color,
    val isUserControlled: Boolean,
    var cards: MutableList<CardType> = mutableListOf(),
    val outposts: MutableList<Outpost> = mutableListOf()
) : Serializable

object RPSRules {
    fun resolve(attacker: UnitType, defender: UnitType): Boolean? {
        if (attacker == defender) return false // دفاعی برتری کا قانون
        return when {
            attacker == UnitType.ROCK && defender == UnitType.SCISSORS -> true
            attacker == UnitType.PAPER && defender == UnitType.ROCK -> true
            attacker == UnitType.SCISSORS && defender == UnitType.PAPER -> true
            else -> false
        }
    }
}
