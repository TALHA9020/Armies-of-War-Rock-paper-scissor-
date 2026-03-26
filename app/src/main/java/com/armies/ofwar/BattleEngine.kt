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

    // 1, 2, 3: گیم سیٹ اپ اور ہزاروں پوسٹس
    fun setupGame(totalPlayers: Int, playerColor: Color) {
        val list = mutableListOf<Army>()
        val colors = listOf(Color.Red, Color.Green, Color.Yellow, Color.Cyan, Color.Magenta, 
                          Color.Blue, Color.White, Color.Gray, Color.DarkGray, Color.LightGray)
        
        for (i in 0 until totalPlayers) {
            val army = Army(i, "Army ${i+1}", if (i == 0) playerColor else colors[i % colors.size], i == 0)
            
            // 3: رینڈم ہزاروں پوسٹس کی تخلیق (مثال کے طور پر یہاں فی آرمی 50 دکھائی گئی ہیں)
            repeat(50) {
                army.outposts.add(Outpost(
                    id = (i * 1000) + it,
                    ownerId = i,
                    units = UnitCounts((5..15).random(), (5..15).random(), (5..15).random()),
                    posX = (0..5000).random().toFloat(),
                    posY = (0..5000).random().toFloat()
                ))
            }
            list.add(army)
        }
        _armies.value = list
    }

    // 4: ڈپلائے ایبل یونٹس (پوسٹس کی تعداد کے حساب سے)
    fun calculateDeployable(armyId: Int): Int {
        val army = _armies.value.find { it.id == armyId }
        return (army?.outposts?.size ?: 0) * 5 
    }

    // 11: ٹریڈ سسٹم
    fun performTrade(armyId: Int): Int {
        val army = _armies.value.find { it.id == armyId }
        if (army != null && army.cards.size >= 3) {
            army.cards.clear()
            return 500 // ٹریڈ پر اضافی یونٹس
        }
        return 0
    }

    // 9: موو یونٹ (پڑوسی پوسٹ پر انتقال)
    fun moveUnits(fromPostId: Int, toPostId: Int, count: UnitCounts) {
        // یہاں منتقلی کی لاجک
    }

    fun endTurn() {
        _currentTurnId.value = (_currentTurnId.value + 1) % _armies.value.size
        // 14: کمپیوٹر کی باری (تیزی کی سطح کے ساتھ)
    }
}
