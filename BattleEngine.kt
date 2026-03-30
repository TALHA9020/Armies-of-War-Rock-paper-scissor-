package com.armies.ofwar

import kotlin.math.max

class BattleEngine {
    fun resolve3vs3Clash(
        aChoices: List<RPSChoice>,
        dChoices: List<RPSChoice>,
        aTroops: Int,
        dTroops: Int
    ): BattleResult {
        var aPoints = 0
        var dPoints = 0

        // Slot by Slot Comparison (1vs1, 2vs2, 3vs3)
        for (i in 0..2) {
            if (aChoices[i] == dChoices[i]) continue
            if (aChoices[i].beats(dChoices[i])) aPoints++ else dPoints++
        }

        // Overall winner based on slots won
        val won = if (aPoints != dPoints) aPoints > dPoints else aTroops > dTroops

        return if (won) {
            // Attacker wins territory, moves half troops
            BattleResult(true, aPoints, dPoints, max(2, aTroops / 2), 0)
        } else {
            // Defender holds, attacker retreats with 1 troop
            BattleResult(false, aPoints, dPoints, 1, max(1, dTroops - 1))
        }
    }
}
