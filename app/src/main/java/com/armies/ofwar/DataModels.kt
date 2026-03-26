package com.armies.ofwar

import androidx.compose.ui.graphics.Color
import java.io.Serializable

// یونٹس کی مختلف اقسام
enum class UnitType {
    ROCK, PAPER, SCISSORS, NONE
}

// گیم کے مختلف مراحل
enum class TurnPhase {
    SELECT_PLAYER_COLOR, // نیا: رنگ منتخب کرنے کا مرحلہ
    SETUP_ENEMIES,       // نیا: دشمنوں کی تعداد منتخب کرنا
    ATTACK, 
    MOVE
}

// ہر چوکی میں موجود یونٹس کی تفصیل
data class UnitCounts(
    var rocks: Int = 0,
    var papers: Int = 0,
    var scissors: Int = 0
) : Serializable {
    val total: Int get() = rocks + papers + scissors
}

// چوکی (Outpost) کا ماڈل
data class Outpost(
    val id: Int,
    val ownerId: Int,
    var units: UnitCounts,
    val posX: Float, // نقشے پر پوزیشن
    val posY: Float
) : Serializable

// فوج (Army) کا اپڈیٹڈ ماڈل
data class Army(
    val id: Int,
    val name: String,
    val color: Color,
    val isUserControlled: Boolean,
    var allianceId: Int,
    val outposts: MutableList<Outpost> = mutableListOf()
) : Serializable

object RPSRules {
    fun resolve(attacker: UnitType, defender: UnitType): Boolean? {
        if (attacker == defender) return null
        return when {
            attacker == UnitType.ROCK && defender == UnitType.SCISSORS -> true
            attacker == UnitType.PAPER && defender == UnitType.ROCK -> true
            attacker == UnitType.SCISSORS && defender == UnitType.PAPER -> true
            else -> false
        }
    }
}
