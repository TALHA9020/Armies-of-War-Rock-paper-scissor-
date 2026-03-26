package com.armies.ofwar

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BattleEngine {
    private val _attackerWave = MutableStateFlow<List<UnitType>>(emptyList())
    val attackerWave: StateFlow<List<UnitType>> = _attackerWave

    private val _defenderWave = MutableStateFlow<List<UnitType>>(emptyList())
    val defenderWave: StateFlow<List<UnitType>> = _defenderWave

    private val engineScope = CoroutineScope(Dispatchers.Default + Job())

    // یونٹ شامل کرنا
    fun addUnitToWave(isAttacker: Boolean, type: UnitType) {
        if (isAttacker) {
            _attackerWave.value = _attackerWave.value + type
        } else {
            _defenderWave.value = _defenderWave.value + type
        }
        processBattle()
    }

    // --- کمپیوٹر (AI) کی لہر شروع کرنے کا فنکشن ---
    fun startAIEnemy() {
        engineScope.launch {
            while (isActive) {
                delay(800) // ہر 0.8 سیکنڈ بعد کمپیوٹر ایک یونٹ بھیجے گا
                
                // کمپیوٹر ہمیشہ ڈیفنڈر (False) کے طور پر کھیلے گا
                val randomUnit = UnitType.values().filter { it != UnitType.NONE }.random()
                addUnitToWave(isAttacker = false, type = randomUnit)
            }
        }
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
        }
    }
}
