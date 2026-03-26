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

    private val _userCards = MutableStateFlow(0)
    val userCards: StateFlow<Int> = _userCards

    private val _currentTurnId = MutableStateFlow(0)
    val currentTurnId: StateFlow<Int> = _currentTurnId

    private var hasCapturedThisTurn = false

    fun setupGame(totalArmies: Int) {
        val list = mutableListOf<Army>()
        val colors = listOf(Color.Cyan, Color.Red, Color.Green, Color.Yellow, Color.Magenta)
        
        for (i in 0 until totalArmies) {
            list.add(
                Army(
                    id = i,
                    name = if (i == 0) "You" else "Enemy $i",
                    color = colors[i % colors.size],
                    isUserControlled = (i == 0),
                    armyCount = 20,
                    allianceId = if (i == 0) 1 else i + 10
                )
            )
        }
        _armies.value = list
        startPassiveIncome()
    }

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

    fun markCaptureSuccess() {
        hasCapturedThisTurn = true
    }

    fun endTurn() {
        if (_currentTurnId.value == 0 && hasCapturedThisTurn) {
            _userCards.value += 1
        }
        hasCapturedThisTurn = false
        _currentTurnId.value = (_currentTurnId.value + 1) % _armies.value.size
        _currentPhase.value = TurnPhase.ATTACK 
    }

    fun exchangeCards() {
        if (_userCards.value >= 4) {
            _userCards.value -= 4
            val currentList = _armies.value.toMutableList()
            currentList[0] = currentList[0].copy(armyCount = currentList[0].armyCount + 20)
            _armies.value = currentList
        }
    }
}
