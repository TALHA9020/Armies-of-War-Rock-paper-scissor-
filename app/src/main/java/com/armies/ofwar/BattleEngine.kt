package com.armies.ofwar

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BattleEngine {
    private val engineScope = CoroutineScope(Dispatchers.Default + Job())
    
    private val _armies = MutableStateFlow<List<Army>>(emptyList())
    val armies: StateFlow<List<Army>> = _armies

    private val _currentPhase = MutableStateFlow(TurnPhase.ATTACK)
    val currentPhase: StateFlow<TurnPhase> = _currentPhase

    private val _currentTurnId = MutableStateFlow(0)
    val currentTurnId: StateFlow<Int> = _currentTurnId

    // 1 & 2: پلیئر کا رنگ اور دشمنوں کی تعداد سیٹ اپ کرنا
    fun setupGame(totalPlayers: Int, playerColor: Color) {
        val list = mutableListOf<Army>()
        val defaultColors = listOf(Color.Red, Color.Green, Color.Yellow, Color.Magenta, Color.Blue, Color.White, Color.Gray)
        
        for (i in 0 until totalPlayers) {
            val isUser = (i == 0)
            val armyColor = if (isUser) playerColor else defaultColors[i % defaultColors.size]
            
            val newArmy = Army(
                id = i,
                name = if (isUser) "You" else "Enemy $i",
                color = armyColor,
                isUserControlled = isUser,
                allianceId = i + 1
            )

            // 3: نقشہ اور چوکیاں بنانا (فرضی کوآرڈینیٹس کے ساتھ)
            // ہر آرمی کو ایک ابتدائی چوکی دینا
            val initialOutpost = Outpost(
                id = i * 100, // منفرد آئی ڈی
                ownerId = i,
                units = UnitCounts(rocks = 10, papers = 10, scissors = 10),
                posX = (100..500).random().toFloat(),
                posY = (200..800).random().toFloat()
            )
            newArmy.outposts.add(initialOutpost)
            
            list.add(newArmy)
        }
        
        _armies.value = list
        startPassiveIncome()
    }

    private fun startPassiveIncome() {
        engineScope.launch {
            while (isActive) {
                delay(2000) // ہر 2 سیکنڈ بعد یونٹس بڑھیں گے
                _armies.value = _armies.value.map { army ->
                    army.copy(outposts = army.outposts.map { outpost ->
                        // ہر چوکی میں برابر یونٹس کا اضافہ
                        outpost.copy(units = outpost.units.copy(
                            rocks = outpost.units.rocks + 1,
                            papers = outpost.units.papers + 1,
                            scissors = outpost.units.scissors + 1
                        ))
                    }.toMutableList())
                }
            }
        }
    }

    // 4: حملے کے لیے چوکیوں کا انتخاب
    private var selectedAttackerOutpost: Outpost? = null

    fun selectOutpost(outpost: Outpost) {
        val currentArmy = _armies.value.find { it.id == _currentTurnId.value }
        
        if (outpost.ownerId == _currentTurnId.value) {
            // اپنی چوکی منتخب کی (اٹیک کے لیے)
            selectedAttackerOutpost = outpost
        } else if (selectedAttackerOutpost != null) {
            // دشمن کی چوکی منتخب کی (حملہ کرنے کے لیے)
            performAttack(selectedAttackerOutpost!!, outpost)
            selectedAttackerOutpost = null
        }
    }

    private fun performAttack(attacker: Outpost, defender: Outpost) {
        // یہاں ہم اگلی فائل (UI) میں اٹیک ویو دکھانے کی لاجک شامل کریں گے
        println("Attack from ${attacker.id} to ${defender.id}")
    }

    fun endTurn() {
        _currentTurnId.value = (_currentTurnId.value + 1) % _armies.value.size
    }
}
