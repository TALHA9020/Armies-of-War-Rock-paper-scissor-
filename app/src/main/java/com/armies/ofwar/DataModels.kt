package com.armies.ofwar

import androidx.compose.ui.graphics.Color
import java.io.Serializable

/**
 * روک، پیپر، سیزر کی اقسام
 */
enum class UnitType {
    ROCK, PAPER, SCISSORS, NONE
}

/**
 * ہر فوج (Army) کی مکمل معلومات
 */
data class Army(
    val id: Int,
    val name: String,
    val color: Color,
    val isUserControlled: Boolean,
    var armyCount: Int = 20,
    var allianceId: Int // جن کا الائنس آئی ڈی ایک ہوگا وہ دوست ہوں گے
) : Serializable

/**
 * مقابلے کے اصول
 */
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
