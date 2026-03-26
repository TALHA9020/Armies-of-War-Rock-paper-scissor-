package com.armies.ofwar

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BattleEngine {
    private val _attackerWave = MutableStateFlow<List<UnitType>>(emptyList())
    val attackerWave: StateFlow<List<UnitType>> = _attackerWave

    private val _defenderWave = MutableStateFlow<List<UnitType>>(emptyList())
    val defenderWave: StateFlow<List<UnitType>> = _defenderWave

    // یونٹ شامل کرنے کا فنکشن
    fun addUnitToWave(isAttacker: Boolean, type: UnitType) {
        if (isAttacker) {
            _attackerWave.value = _attackerWave.value + type
        } else {
            _defenderWave.value = _defenderWave.value + type
        }
        processBattle()
    }

    private fun processBattle() {
        val atkList = _attackerWave.value.toMutableList()
        val defList = _defenderWave.value.toMutableList()

        if (atkList.isNotEmpty() && defList.isNotEmpty()) {
            val atkUnit = atkList.first()
            val defUnit = defList.first()

            // روک، پیپر، سیزر کا فیصلہ
            val attackerWins = RPSRules.resolve(atkUnit, defUnit)

            when (attackerWins) {
                true -> {
                    // صرف اس صورت میں اٹیکر جیتے گا اگر اس کا یونٹ بھاری ہو
                    defList.removeAt(0)
                }
                false -> {
                    // ڈیفنڈر کا یونٹ بھاری تھا، اٹیکر کا ختم
                    atkList.removeAt(0)
                }
                null -> {
                    // برابر (Tie): آپ کے اصول کے مطابق ڈیفنڈر بھاری پڑے گا
                    // یعنی اٹیکر کا یونٹ ضائع ہو جائے گا، ڈیفنڈر کا محفوظ رہے گا
                    atkList.removeAt(0)
                }
            }
            
            _attackerWave.value = atkList
            _defenderWave.value = defList
        }
    }

    // ٹیسٹنگ کے لیے یا جنگ ختم ہونے پر لہریں صاف کرنا
    fun clearWaves() {
        _attackerWave.value = emptyList()
        _defenderWave.value = emptyList()
    }
}
