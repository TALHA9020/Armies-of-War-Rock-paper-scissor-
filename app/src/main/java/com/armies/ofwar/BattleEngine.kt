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

    // 1 & 2: پلیئر کا رنگ اور دشمنوں کی تعداد کے ساتھ گیم سیٹ اپ
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

            // 3: نقشہ اور چوکیاں (رینڈم پوزیشنز)
            val initialOutpost = Outpost(
                id = i * 100,
                ownerId = i,
                units = UnitCounts(rocks = 10, papers = 10, scissors = 10),
                posX = (200..800).random().toFloat(),
                posY = (400..1200).random().toFloat()
            )
            newArmy.outposts.add(initialOutpost)
            list.add(newArmy)
        }
        
        _armies.value = list
        startPassiveIncome()
    }

    // ہر 2 سیکنڈ بعد چوکیوں میں یونٹس کا خودکار اضافہ
    private fun startPassiveIncome() {
        engineScope.launch {
            while (isActive) {
                delay(2000)
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

    // جنگی مقابلہ اور RPS قوانین کا نفاذ
    fun executeBattle(attackerOutpost: Outpost, defenderOutpost: Outpost, attackType: UnitType) {
        // دفاع کرنے والا ہمیشہ بہترین یا رینڈم یونٹ استعمال کرے گا
        val defenderUnits = defenderOutpost.units
        val defenseType = when {
            defenderUnits.rocks > 0 -> UnitType.ROCK
            defenderUnits.papers > 0 -> UnitType.PAPER
            else -> UnitType.SCISSORS
        }

        // RPS قوانین: کون جیتا؟
        val attackWon = RPSRules.resolve(attackType, defenseType)

        _armies.value = _armies.value.map { army ->
            army.copy(outposts = army.outposts.map { outpost ->
                if (outpost.id == defenderOutpost.id) {
                    val updatedUnits = outpost.units.copy()
                    // اگر حملہ آور جیتا تو دفاعی یونٹس کم ہوں گے
                    if (attackWon == true) {
                        when(defenseType) {
                            UnitType.ROCK -> updatedUnits.rocks = (updatedUnits.rocks - 5).coerceAtLeast(0)
                            UnitType.PAPER -> updatedUnits.papers = (updatedUnits.papers - 5).coerceAtLeast(0)
                            UnitType.SCISSORS -> updatedUnits.scissors = (updatedUnits.scissors - 5).coerceAtLeast(0)
                            else -> {}
                        }
                    }
                    // اگر برابر رہا یا دفاعی جیتا تو نقصان نہیں ہوگا (دفاعی حاوی ہے)
                    outpost.copy(units = updatedUnits)
                } else outpost
            }.toMutableList())
        }
    }

    // لہر ختم ہونے پر یونٹس کی واپسی (Recovery Rule)
    fun handleWaveConclusion(attackerId: Int, attackSuccessful: Boolean) {
        _armies.value = _armies.value.map { army ->
            if (army.id == attackerId) {
                // اگر حملہ مکمل ہوا تو 70% یونٹس واپس پہلی چوکی (HQ) میں
                val recoveryRate = if (attackSuccessful) 0.7f else 0.3f
                army.copy(outposts = army.outposts.mapIndexed { index, outpost ->
                    if (index == 0) {
                        outpost.copy(units = outpost.units.copy(
                            rocks = outpost.units.rocks + (5 * recoveryRate).toInt(),
                            papers = outpost.units.papers + (5 * recoveryRate).toInt(),
                            scissors = outpost.units.scissors + (5 * recoveryRate).toInt()
                        ))
                    } else outpost
                }.toMutableList())
            } else army
        }
    }

    fun endTurn() {
        _currentTurnId.value = (_currentTurnId.value + 1) % _armies.value.size
        // اگر اگلی باری دشمن کی ہے تو AI لاجک چلائیں
        if (_currentTurnId.value != 0) {
            performEnemyMove()
        }
    }

    private fun performEnemyMove() {
        engineScope.launch {
            delay(2000)
            // دشمن ایک رینڈم حملہ کرے گا (UI اس لہر کو دکھائے گا)
            endTurn()
        }
    }
}
