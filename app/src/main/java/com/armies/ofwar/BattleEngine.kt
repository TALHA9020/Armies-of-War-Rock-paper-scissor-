package com.armies.ofwar

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BattleEngine {
    private val engineScope = CoroutineScope(Dispatchers.Default + Job())
    private val _armies = MutableStateFlow<List<Army>>(emptyList())
    val armies: StateFlow<List<Army>> = _armies

    private val _currentTurnId = MutableStateFlow(0)
    val currentTurnId: StateFlow<Int> = _currentTurnId

    // 1, 2, 3: گیم سیٹ اپ (10 رنگوں کی گنجائش اور رینڈم پوسٹس)
    fun setupGame(totalPlayers: Int, playerColor: Color) {
        val list = mutableListOf<Army>()
        val colors = listOf(Color.Red, Color.Green, Color.Yellow, Color.Cyan, Color.Magenta, 
                          Color.Blue, Color.White, Color.Gray, Color.DarkGray, Color.LightGray)
        
        for (i in 0 until totalPlayers) {
            val army = Army(i, "Army ${i+1}", if (i == 0) playerColor else colors[i % colors.size], i == 0)
            repeat(20) { 
                army.outposts.add(Outpost(
                    id = (i * 1000) + it,
                    ownerId = i,
                    units = UnitCounts((10..20).random(), (10..20).random(), (10..20).random()),
                    posX = (100..5000).random().toFloat(),
                    posY = (100..5000).random().toFloat()
                ))
            }
            list.add(army)
        }
        _armies.value = list
    }

    // 11: ٹریڈ بونس لاجک
    fun performTrade(armyId: Int): Int {
        val army = _armies.value.find { it.id == armyId } ?: return 0
        if (army.cards.size < 3) return 0
        
        val distinctCards = army.cards.take(3).distinct().size
        army.cards = army.cards.drop(3).toMutableList()
        
        // 11: 3 ایک جیسے یا 3 مختلف کارڈز کا ٹریڈ بونس
        return if (distinctCards == 1) 500 else 1000 
    }

    // 13: کہکشاں سے کائنات تک کی پروگریس لاجک
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

    fun executeBattle(attacker: Outpost, defender: Outpost, attackType: UnitType) {
        // یہاں جنگ اور کارڈ ملنے کی لاجک آئے گی
        endTurn()
    }

    fun endTurn() {
        val nextTurn = (_currentTurnId.value + 1) % _armies.value.size
        _currentTurnId.value = nextTurn
        _armies.value.find { it.id == nextTurn }?.let { updateTerritoryLevel(it) }
    }
}
