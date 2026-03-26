package com.armies.ofwar

import java.io.Serializable

/**
 * روک، پیپر، سیزر کی اقسام
 */
enum class UnitType {
    ROCK, PAPER, SCISSORS, NONE
}

/**
 * جنگ کی ایک لہر (Wave) جو بٹن دبانے سے بنتی ہے
 */
data class BattleUnit(
    val type: UnitType,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * علاقے کی معلومات
 */
data class Territory(
    val id: Int,
    val name: String,
    var owner: PlayerType,
    var armyCount: Int,
    val neighbors: List<Int>,
    // اس علاقے کی موجودہ جنگی لہر
    val currentWave: MutableList<BattleUnit> = mutableListOf()
) : Serializable

enum class PlayerType {
    USER, ENEMY, NEUTRAL
}

/**
 * مقابلے کا نتیجہ نکالنے کے لیے رولز
 */
object RPSRules {
    fun isWinner(attacker: UnitType, defender: UnitType): Boolean {
        return when {
            attacker == UnitType.ROCK && defender == UnitType.SCISSORS -> true
            attacker == UnitType.PAPER && defender == UnitType.ROCK -> true
            attacker == UnitType.SCISSORS && defender == UnitType.PAPER -> true
            else -> false // اگر برابر ہو یا ڈیفنڈر جیتے
        }
    }
}
