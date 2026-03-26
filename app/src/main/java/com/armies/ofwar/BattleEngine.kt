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

    private val _currentPhase = MutableStateFlow(TurnPhase.ATTACK)
    val currentPhase: StateFlow<TurnPhase> = _currentPhase

    private val _userCards = MutableStateFlow(0)
    val userCards: StateFlow<Int> = _userCards

    private val _attackerWave = MutableStateFlow<List<UnitType>>(emptyList())
    val attackerWave: StateFlow<List<UnitType>> = _attackerWave

    private val _defenderWave = MutableStateFlow<List<UnitType>>(emptyList())
    val defenderWave: StateFlow<List<UnitType>> = _defenderWave

    private var didCaptureInThisTurn = false

    fun setupGame(totalArmies: Int, userAllianceWith: List<Int>) {
        val colors = listOf(Color.Cyan, Color.Red, Color.Green, Color.Yellow, Color.Magenta, 
                           Color.White, Color.Gray, Color.Blue, Color.LightGray, Color.DarkGray)
        
        val newArmies = (0 until totalArmies).map { id ->
            Army(
                id = id,
                name = if (id == 0) "You" else "Army ${id + 1}",
                color = colors[id % colors.size],
                isUserControlled = (id == 0),
                allianceId = if (id == 0 || userAllianceWith.contains(id)) 1 else id + 10 
            )
        }
        _armies.value = newArmies
        startPassiveIncome()
        startAILogic()
    }

    // ہر سیکنڈ ہر چوکی میں 1 یونٹ کا اضافہ
    private fun startPassiveIncome() {
        engineScope.launch {
            while (isActive) {
                delay(1000)
                _armies.value = _armies.value.map { it.copy(armyCount = it.armyCount + 1) }
            }
        }
    }

    fun setPhase(phase: TurnPhase) {
        _currentPhase.value = phase
    }

    fun addUnitToWave(isAttacker: Boolean, type: UnitType) {
        val currentList = _armies.value.toMutableList()
        val actorId = if (isAttacker) _currentTurnId.value else getDefenderId()
        
        if (actorId != -1 && currentList[actorId].armyCount > 0) {
            if (_currentPhase.value == TurnPhase.MOVE && isAttacker) {
                // MOVE فیز میں یونٹ براہ راست دوسری چوکی (اتحادی) کو منتقل ہوتی ہے
                moveToAlly(type)
            } else {
                currentList[actorId] = currentList[actorId].copy(armyCount = currentList[actorId].armyCount - 1)
                _armies.value = currentList
                if (isAttacker) _attackerWave.value += type else _defenderWave.value += type
                processBattle()
            }
        }
    }

    private fun moveToAlly(type: UnitType) {
        val allyId = _armies.value.indexOfFirst { it.id != 0 && it.allianceId == 1 }
        if (allyId != -1) updateArmyCount(allyId, 1)
    }

    private fun processBattle() {
        val atk = _attackerWave.value.toMutableList()
        val def = _defenderWave.value.toMutableList()

        if (atk.isNotEmpty() && def.isNotEmpty()) {
            val result = RPSRules.resolve(atk.first(), def.first())
            if (result == true) {
                def.removeAt(0)
                didCaptureInThisTurn = true // کامیاب حملے کی نشانی
            } else atk.removeAt(0)
            
            _attackerWave.value = atk
            _defenderWave.value = def
            checkAndReturnUnits()
        }
    }

    private fun checkAndReturnUnits() {
        if (_defenderWave.value.isEmpty() && _attackerWave.value.isNotEmpty()) {
            updateArmyCount(_currentTurnId.value, _attackerWave.value.size)
            _attackerWave.value = emptyList()
        }
    }

    fun endTurn() {
        if (didCaptureInThisTurn && _currentTurnId.value == 0) {
            _userCards.value += 1 // کارڈ ملنا
        }
        didCaptureInThisTurn = false
        _currentTurnId.value = (_currentTurnId.value + 1) % _armies.value.size
    }

    fun exchangeCards() {
        if (_userCards.value >= 4) {
            _userCards.value -= 4
            updateArmyCount(0, 20)
        }
    }

    private fun updateArmyCount(armyId: Int, count: Int) {
        val list = _armies.value.toMutableList()
        list[armyId] = list[armyId].copy(armyCount = list[armyId].armyCount + count)
        _armies.value = list
    }

    private fun getDefenderId() = _armies.value.indexOfFirst { it.allianceId != 1 && it.armyCount > 0 }

    private fun startAILogic() {
        engineScope.launch {
            while (isActive) {
                if (_currentTurnId.value != 0) {
                    delay(2000)
                    addUnitToWave(true, UnitType.values().filter { it != UnitType.NONE }.random())
                    delay(1000)
                    endTurn()
                }
                delay(500)
            }
        }
    }
}
