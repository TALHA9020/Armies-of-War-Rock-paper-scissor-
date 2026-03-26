package com.armies.ofwar

import java.io.Serializable

/**
 * گیم کے ہر علاقے (Territory) کی معلومات
 */
data class Territory(
    val id: Int,
    val name: String,
    var owner: PlayerType,
    var armyCount: Int,
    val neighbors: List<Int> // ان علاقوں کے IDs جن سے یہ جڑا ہوا ہے
) : Serializable

/**
 * کھلاڑی کی اقسام
 */
enum class PlayerType {
    USER,      // آپ (نیلا رنگ)
    ENEMY,     // دشمن (سرخ رنگ)
    NEUTRAL    // خالی علاقہ (سرمئی رنگ)
}

/**
 * جنگ کے نتیجے کا ڈیٹا
 */
data class BattleResult(
    val attackerWon: Boolean,
    val remainingAttackerArmies: Int,
    val remainingDefenderArmies: Int
)
