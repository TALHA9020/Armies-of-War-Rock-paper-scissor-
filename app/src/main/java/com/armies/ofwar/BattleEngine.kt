package com.armies.ofwar

class BattleEngine {
    
    fun resolveBattle(
        attackerTroops: Int, 
        defenderTroops: Int, 
        attackerChoices: List<RPSChoice>, 
        defenderChoices: List<RPSChoice>
    ): BattleResult {
        var attackerWins = 0
        var defenderWins = 0

        // 3 Rounds of RPS
        for (i in 0 until 3) {
            val a = attackerChoices[i]
            val d = defenderChoices[i]
            
            if (a == d) continue // Tie in round
            if (a.beats(d)) attackerWins++ else defenderWins++
        }

        return when {
            attackerWins > defenderWins -> BattleResult(true, "Attacker won RPS $attackerWins-$defenderWins")
            defenderWins > attackerWins -> BattleResult(false, "Defender won RPS $defenderWins-$attackerWins")
            else -> {
                // Tie-breaker: More units win
                if (attackerTroops > defenderTroops) {
                    BattleResult(true, "RPS Tie! Attacker wins by troop count ($attackerTroops vs $defenderTroops)")
                } else {
                    BattleResult(false, "RPS Tie! Defender wins by troop count ($defenderTroops vs $attackerTroops)")
                }
            }
        }
    }

    data class BattleResult(val attackerWon: Boolean, val message: String)
}
