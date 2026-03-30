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

        // 3 راؤنڈز کا موازنہ
        for (i in 0 until 3) {
            val a = aChoices[i]
            val d = dChoices[i]
            if (a == d) continue
            if (a.beats(d)) aPoints++ else dPoints++
        }

        // فاتح کا فیصلہ (پوائنٹس کی بنیاد پر، برابر ہونے پر زیادہ فوج جیتتی ہے)
        val attackerWon = if (aPoints != dPoints) aPoints > dPoints else aTroops > dTroops

        return if (attackerWon) {
            // حملہ آور جیتا: آدھی فوج وہاں منتقل، دشمن ختم
            BattleResult(true, max(2, aTroops / 2), 1, "فتح! علاقہ فتح کر لیا گیا۔")
        } else {
            // دفاع کرنے والا جیتا: حملہ آور واپس (1 فوج کے ساتھ)
            BattleResult(false, 1, max(1, dTroops - 1), "شکست! دشمن نے دفاع کر لیا۔")
        }
    }
}
