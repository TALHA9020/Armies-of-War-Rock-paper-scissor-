package com.armies.ofwar

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BattleEngine {
    private val _armies = MutableStateFlow<List<Army>>(emptyList())
    val armies: StateFlow<List<Army>> = _armies

    private val _currentTurnId = MutableStateFlow(0)
    val currentTurnId: StateFlow<Int> = _currentTurnId

    // ڈپلائے ایبل یونٹس کی تفصیل
    var rLeft = mutableStateOf(5)
    var pLeft = mutableStateOf(5)
    var sLeft = mutableStateOf(5)

    fun setupGame(totalPlayers: Int, playerColor: Color) {
        val list = mutableListOf<Army>()
        val colors = listOf(Color.Cyan, Color.Red, Color.Green, Color.Yellow, Color.Magenta, Color.Blue, Color.White, Color.Gray, Color(0xFFFFA500), Color(0xFF4CAF50))
        for (i in 0 until totalPlayers) {
            val army = Army(i, if(i==0) "آپ" else "دشمن $i", if (i == 0) playerColor else colors[i % colors.size], i == 0)
            repeat(5) { 
                army.outposts.add(Outpost(id = (i * 1000) + it, ownerId = i, 
                    units = UnitCounts(10, 10, 10),
                    posX = (300..1500).random().toFloat(), posY = (400..2500).random().toFloat()
                ))
            }
            list.add(army)
        }
        _armies.value = list
    }

    fun deploySpecific(postId: Int, type: UnitType) {
        _armies.value = _armies.value.map { army ->
            army.copy(outposts = army.outposts.map { post ->
                if (post.id == postId) {
                    when(type) {
                        UnitType.ROCK -> { if(rLeft.value > 0) { rLeft.value--; post.units.rocks++ } }
                        UnitType.PAPER -> { if(pLeft.value > 0) { pLeft.value--; post.units.papers++ } }
                        UnitType.SCISSORS -> { if(sLeft.value > 0) { sLeft.value--; post.units.scissors++ } }
                        else -> {}
                    }
                    post
                } else post
            }.toMutableList())
        }
    }

    fun endTurn() {
        _currentTurnId.value = (_currentTurnId.value + 1) % _armies.value.size
        rLeft.value = 5; pLeft.value = 5; sLeft.value = 5
    }
}
