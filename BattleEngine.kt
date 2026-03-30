package com.armies.ofwar

import kotlin.math.max

class BattleEngine {
    
    fun resolveBattle(
        attackerChoices: List<RPSChoice>,
        defenderChoices: List<RPSChoice>,
        attackerTroops: Int,
        defenderTroops: Int
    ): BattleResult {
        var aWins = 0
        var dWins = 0

        // RPS Rounds
        for (i in 0 until 3) {
            if (attackerChoices[i] == defenderChoices[i]) continue
            if (attackerChoices[i].beats(defenderChoices[i])) aWins++ else dWins++
        }

        return if (aWins > dWins || (aWins == dWins && attackerTroops > defenderTroops)) {
            // Attacker Wins the territory
            BattleResult(
                attackerWon = true,
                message = "فتح! آپ نے علاقہ فتح کر لیا۔",
                attackerRemaining = max(1, attackerTroops - 2), // کچھ فوج ضائع ہوئی
                defenderRemaining = 0
            )
        } else {
            // Defender Wins
            BattleResult(
                attackerWon = false,
                message = "شکست! دفاعی فوج جیت گئی۔",
                attackerRemaining = 1, // صرف ایک یونٹ واپس بچا
                defenderRemaining = max(1, defenderTroops - 1)
            )
        }
    }
}
