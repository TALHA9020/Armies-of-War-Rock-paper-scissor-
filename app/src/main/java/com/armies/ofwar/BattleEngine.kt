package com.armies.ofwar

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BattleEngine {
    // فوج کی کل تعداد
    val userArmyCount = MutableStateFlow(20)
    val enemyArmyCount = MutableStateFlow(20)

    private val _attackerWave = MutableStateFlow<List<UnitType>>(emptyList())
    val attackerWave: StateFlow<List<UnitType>> = _attackerWave

    private val _defenderWave = MutableStateFlow<List<UnitType>>(emptyList())
    val defenderWave: StateFlow<List<UnitType>> = _defenderWave

    private val engineScope = CoroutineScope(Dispatchers.Default + Job())

    fun addUnitToWave(isAttacker: Boolean, type: UnitType) {
        if (isAttacker) {
            if (userArmyCount.value > 0) {
                userArmyCount.value -= 1 // چوکی سے یونٹ نکلا
                _attackerWave.value = _attackerWave.value + type
            }
        } else {
            if (enemyArmyCount.value > 0) {
                enemyArmyCount.value -= 1 // دشمن کی چوکی سے یونٹ نکلا
                _defenderWave.value = _defenderWave.value + type
            }
        }
        processBattle()
    }

    private fun processBattle() {
        val atkList = _attackerWave.value.toMutableList()
        val defList = _defenderWave.value.toMutableList()

        if (atkList.isNotEmpty() && defList.isNotEmpty()) {
            val result = RPSRules.resolve(atkList.first(), defList.first())
            when (result) {
                true -> defList.removeAt(0) // اٹیکر جیتا
                false -> atkList.removeAt(0) // ڈیفنڈر جیتا
                null -> atkList.removeAt(0)  // برابر (ڈیفنڈر ایڈوانٹیج)
            }
            
            _attackerWave.value = atkList
            _defenderWave.value = defList

            // جب ایک طرف کی لہر ختم ہو جائے تو چیک کریں
            checkAndReturnUnits()
        }
    }

    private fun checkAndReturnUnits() {
        // اگر دشمن کی لہر ختم ہو گئی، تو آپ کی باقی یونٹس واپس چوکی میں
        if (_defenderWave.value.isEmpty() && _attackerWave.value.isNotEmpty()) {
            userArmyCount.value += _attackerWave.value.size
            _attackerWave.value = emptyList()
        }
        // اگر آپ کی لہر ختم ہو گئی، تو دشمن کی باقی یونٹس اس کی چوکی میں واپس
        else if (_attackerWave.value.isEmpty() && _defenderWave.value.isNotEmpty()) {
            enemyArmyCount.value += _defenderWave.value.size
            _defenderWave.value = emptyList()
        }
    }

    fun startAIEnemy() {
        engineScope.launch {
            while (isActive) {
                delay(1200) // دشمن کے حملے کا وقفہ
                if (enemyArmyCount.value > 0) {
                    val randomUnit = UnitType.values().filter { it != UnitType.NONE }.random()
                    addUnitToWave(false, randomUnit)
                }
            }
        }
    }
}
