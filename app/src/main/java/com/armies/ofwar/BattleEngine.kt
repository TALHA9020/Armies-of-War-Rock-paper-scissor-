package com.armies.ofwar

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BattleEngine {
    private val _armies = MutableStateFlow<List<Army>>(emptyList())
    val armies: StateFlow<List<Army>> = _armies

    private val _currentTurnId = MutableStateFlow(0)
    val currentTurnId: StateFlow<Int> = _currentTurnId

    // 1, 2: گیم سیٹ اپ اور آرمیز کا انتخاب
    fun setupGame(totalPlayers: Int, playerColor: Color) {
        val list = mutableListOf<Army>()
        val colors = listOf(Color.Red, Color.Green, Color.Yellow, Color.Cyan, Color.Magenta, Color.Blue, Color.White, Color.Gray)
        
        for (i in 0 until totalPlayers) {
            val army = Army(i, "Army ${i+1}", if (i == 0) playerColor else colors[i % colors.size], i == 0)
            repeat(20) { // 3: ہزاروں پوسٹس کا آغاز (یہاں 20 سے مثال دی گئی ہے)
                army.outposts.add(Outpost(
                    id = (i * 1000) + it,
                    ownerId = i,
                    units = UnitCounts((15..25).random(), (15..25).random(), (15..25).random()),
                    posX = (100..5000).random().toFloat(),
                    posY = (100..5000).random().toFloat()
                ))
            }
            list.add(army)
        }
        _armies.value = list
    }

    // 5: اٹیک کے لیے 10 یونٹس کی شرط
    fun canInitiateAttack(outpost: Outpost): Boolean {
        return outpost.units.total >= 10
    }

    // 11: کارڈ ٹریڈنگ لاجک
    fun performTrade(armyId: Int): Int {
        val army = _armies.value.find { it.id == armyId } ?: return 0
        if (army.cards.size < 3) return 0
        val distinctCards = army.cards.take(3).distinct().size
        army.cards = army.cards.drop(3).toMutableList()
        return if (distinctCards == 1) 500 else 1000
    }

    // 13: پروگریس لیول اپ گریڈ
    fun updateTerritoryLevel(army: Army) {
        val count = army.outposts.size
        val newLevel = when {
            count >= 1000 -> TerritoryLevel.UNIVERSE
            count >= 500 -> TerritoryLevel.GALAXY
            count >= 100 -> TerritoryLevel.PLANET
            count >= 50 -> TerritoryLevel.COUNTRY
            count >= 10 -> TerritoryLevel.CITY
            else -> TerritoryLevel.POST
        }
        army.outposts.forEach { it.level = newLevel }
    }

    fun endTurn() {
        val nextTurn = (_currentTurnId.value + 1) % _armies.value.size
        _currentTurnId.value = nextTurn
        _armies.value.find { it.id == nextTurn }?.let { updateTerritoryLevel(it) }
    }
}
