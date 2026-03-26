package com.armies.ofwar

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BattleEngine {
    // اٹیکر اور ڈیفنڈر کی موجودہ لہریں
    private val _attackerWave = MutableStateFlow<List<UnitType>>(emptyList())
    val attackerWave: StateFlow<List<UnitType>> = _attackerWave

    private val _defenderWave = MutableStateFlow<List<UnitType>>(emptyList())
    val defenderWave: StateFlow<List<UnitType>> = _defenderWave

    // جب کھلاڑی بٹن دبائے گا تو یونٹ شامل ہوگا
    fun addUnitToWave(isAttacker: Boolean, type: UnitType) {
        if (isAttacker) {
            _attackerWave.value = _attackerWave.value + type
        } else {
            _defenderWave.value = _defenderWave.value + type
        }
        processBattle()
    }

    // لہروں کا آپس میں مقابلہ
    private fun processBattle() {
        val atkList = _attackerWave.value.toMutableList()
        val defList = _defenderWave.value.toMutableList()

        // اگر دونوں طرف یونٹس موجود ہوں تو مقابلہ کرائیں
        if (atkList.isNotEmpty() && defList.isNotEmpty()) {
            val atkUnit = atkList.first()
            val defUnit = defList.first()

            val result = RPSRules.resolve(atkUnit, defUnit)

            when (result) {
                true -> { // اٹیکر جیتا، ڈیفنڈر کا یونٹ ختم
                    defList.removeAt(0)
                }
                false -> { // ڈیفنڈر جیتا، اٹیکر کا یونٹ ختم
                    atkList.removeAt(0)
                }
                null -> { // برابر، دونوں ختم
                    atkList.removeAt(0)
                    defList.removeAt(0)
                }
            }
            
            _attackerWave.value = atkList
            _defenderWave.value = defList
        }
    }
}
