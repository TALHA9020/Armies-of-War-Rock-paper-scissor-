package com.armies.ofwar

import androidx.compose.ui.graphics.Color
import java.io.Serializable

// 7: یونٹس کی اقسام
enum class UnitType { ROCK, PAPER, SCISSORS, NONE }

// 13: درجہ بندی کا نظام
enum class TerritoryLevel { POST, CITY, PROVINCE, COUNTRY, CONTINENT, PLANET, SOLAR_SYSTEM, GALAXY, UNIVERSE }

// 10: کارڈ سسٹم
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
    var cards: MutableList<CardType> = mutableListOf(), // 12: میکس 5 کارڈز
    val outposts: MutableList<Outpost> = mutableListOf()
) : Serializable

object RPSRules {
    fun resolve(attacker: UnitType, defender: UnitType): Boolean? {
        if (attacker == defender) return false // 8: سیم یونٹ پر ڈیفنڈر بھاری (اٹیکر مر جائے گا)
        return when {
            attacker == UnitType.ROCK && defender == UnitType.SCISSORS -> true
            attacker == UnitType.PAPER && defender == UnitType.ROCK -> true
            attacker == UnitType.SCISSORS && defender == UnitType.PAPER -> true
            else -> false
        }
    }
}
