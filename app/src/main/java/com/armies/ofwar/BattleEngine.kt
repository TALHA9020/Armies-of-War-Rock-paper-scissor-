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

    // گیم سیٹ اپ: پلیئر کا رنگ اور دشمنوں کی تعداد
    fun setupGame(totalPlayers: Int, playerColor: Color) {
        val list = mutableListOf<Army>()
        val defaultColors = listOf(Color.Red, Color.Green, Color.Yellow, Color.Magenta, Color.Blue, Color.Gray)
        
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

            // نقشے پر چوکیوں کی پوزیشن (رینڈم لیکن اسکرین کے اندر)
            val initialOutpost = Outpost(
                id = i * 100,
                ownerId = i,
                units = UnitCounts(rocks = 20, papers = 20, scissors = 20),
                posX = (200..800).random().toFloat(),
                posY = (400..1200).random().toFloat()
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
                delay(3000) // ہر 3 سیکنڈ بعد یونٹس میں اضافہ
                _armies.value = _armies.value.map { army ->
                    army.copy(outposts = army.outposts.map { outpost ->
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

    // جنگی لاجک: آپ کا بتایا ہوا اصول (سیم یونٹ پر ڈیفنڈر حاوی)
    fun executeBattle(attacker: Outpost, defender: Outpost, attackType: UnitType) {
        // دشمن کا دفاعی انتخاب (رینڈم لیکن موجودہ یونٹس میں سے)
        val defenseType = listOf(UnitType.ROCK, UnitType.PAPER, UnitType.SCISSORS).random()

        // RPS قوانین کا استعمال
        val attackWon = RPSRules.resolve(attackType, defenseType)

        _armies.value = _armies.value.map { army ->
            army.copy(outposts = army.outposts.map { outpost ->
                if (outpost.id == defender.id) {
                    val updatedUnits = outpost.units.copy()
                    
                    // اصول: اگر حملہ جیت گیا تو دفاعی یونٹس کم ہوں گے
                    if (attackWon == true) {
                        when(defenseType) {
                            UnitType.ROCK -> updatedUnits.rocks = (updatedUnits.rocks - 10).coerceAtLeast(0)
                            UnitType.PAPER -> updatedUnits.papers = (updatedUnits.papers - 10).coerceAtLeast(0)
                            UnitType.SCISSORS -> updatedUnits.scissors = (updatedUnits.scissors - 10).coerceAtLeast(0)
                            else -> {}
                        }
                    } 
                    // اصول: اگر ڈرا ہوا یا دفاعی جیت گیا، تو کچھ نہیں ہوگا (ڈیفنڈر حاوی ہے)
                    
                    outpost.copy(units = updatedUnits)
                } else outpost
            }.toMutableList())
        }
        
        // باری ختم کریں اور اگلے کھلاڑی کو موقع دیں
        endTurn()
    }

    fun handleWaveConclusion(attackerId: Int, isSuccess: Boolean) {
        // لہر مٹنے پر یونٹس کی واپسی کی لاجک یہاں آئے گی
        // فی الحال یہ سسٹم کو مستحکم رکھنے کے لیے سادہ رکھی گئی ہے
    }

    fun endTurn() {
        val nextTurn = (_currentTurnId.value + 1) % _armies.value.size
        _currentTurnId.value = nextTurn
    }
}
