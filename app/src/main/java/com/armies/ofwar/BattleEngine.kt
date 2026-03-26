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

    private val _attackerWave = MutableStateFlow<List<UnitType>>(emptyList())
    val attackerWave: StateFlow<List<UnitType>> = _attackerWave

    private val _defenderWave = MutableStateFlow<List<UnitType>>(emptyList())
    val defenderWave: StateFlow<List<UnitType>> = _defenderWave

    // گیم سیٹ اپ: افواج اور اتحاد
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
        _currentTurnId.value = 0
        startAILogic()
    }

    fun addUnitToWave(isAttacker: Boolean, type: UnitType) {
        val currentList = _armies.value.toMutableList()
        val actorId = if (isAttacker) _currentTurnId.value else getDefenderId()
        
        if (actorId != -1 && currentList[actorId].armyCount > 0) {
            currentList[actorId] = currentList[actorId].copy(armyCount = currentList[actorId].armyCount - 1)
            _armies.value = currentList
            
            if (isAttacker) _attackerWave.value += type 
            else _defenderWave.value += type
            
            processBattle()
        }
    }

    private fun getDefenderId(): Int {
        val attacker = _armies.value.getOrNull(_currentTurnId.value) ?: return -1
        return _armies.value.indexOfFirst { it.allianceId != attacker.allianceId && it.armyCount > 0 }
    }

    private fun processBattle() {
        val atk = _attackerWave.value.toMutableList()
        val def = _defenderWave.value.toMutableList()

        if (atk.isNotEmpty() && def.isNotEmpty()) {
            val result = RPSRules.resolve(atk.first(), def.first())
            when (result) {
                true -> def.removeAt(0)
                false -> atk.removeAt(0)
                null -> atk.removeAt(0)
            }
            _attackerWave.value = atk
            _defenderWave.value = def
            checkAndReturnUnits()
        }
    }

    private fun checkAndReturnUnits() {
        if (_defenderWave.value.isEmpty() && _attackerWave.value.isNotEmpty()) {
            updateArmyCount(_currentTurnId.value, _attackerWave.value.size)
            _attackerWave.value = emptyList()
        } else if (_attackerWave.value.isEmpty() && _defenderWave.value.isNotEmpty()) {
            updateArmyCount(getDefenderId(), _defenderWave.value.size)
            _defenderWave.value = emptyList()
        }
    }

    private fun updateArmyCount(armyId: Int, count: Int) {
        if (armyId == -1) return
        val list = _armies.value.toMutableList()
        list[armyId] = list[armyId].copy(armyCount = list[armyId].armyCount + count)
        _armies.value = list
    }

    private fun startAILogic() {
        engineScope.launch {
            while (isActive) {
                val currentArmy = _armies.value.getOrNull(_currentTurnId.value)
                if (currentArmy != null && !currentArmy.isUserControlled) {
                    delay(1000)
                    addUnitToWave(true, UnitType.values().filter { it != UnitType.NONE }.random())
                }
                delay(500)
            }
        }
    }
}
