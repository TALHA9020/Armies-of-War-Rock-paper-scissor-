package com.armies.ofwar

class BattleEngine {
    
    fun resolveBattle(
        attackerChoices: List<RPSChoice>,
        defenderChoices: List<RPSChoice>,
        attackerTroops: Int,
        defenderTroops: Int
    ): BattleResult {
        var attackerWins = 0
        var defenderWins = 0

        // 3 Rounds of RPS logic
        for (i in 0 until 3) {
            val a = attackerChoices[i]
            val d = defenderChoices[i]
            if (a == d) continue
            if (a.beats(d)) attackerWins++ else defenderWins++
        }

        val won = when {
            attackerWins > defenderWins -> true
            defenderWins > attackerWins -> false
            else -> attackerTroops > defenderTroops // Tie breaker
        }

        return if (won) {
            // حملہ آور جیت گیا: دفاعی فوج ختم، حملہ آور کی کچھ فوج وہاں منتقل ہوگی
            BattleResult(true, "فتح! علاقہ آپ کا ہوا۔", remainingAttackerTroops = attackerTroops - 1)
        } else {
            // دفاع جیت گیا: حملہ آور کی بھیجی گئی فوج ضائع ہوگئی
            BattleResult(false, "شکست! دفاعی فوج مضبوط نکلی۔", remainingAttackerTroops = 1)
        }
    }

    data class BattleResult(
        val attackerWon: Boolean, 
        val message: String, 
        val remainingAttackerTroops: Int
    )
}
