package com.armies.ofwar

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BattleEngine {
    private val _armies = MutableStateFlow<List<Army>>(emptyList())
    val armies: StateFlow<List<Army>> = _armies

    private val _currentTurnId = MutableStateFlow(0)
    val currentTurnId: StateFlow<Int> = _currentTurnId

    fun setupGame(totalPlayers: Int, playerColor: Color) {
        val list = mutableListOf<Army>()
        val colors = listOf(Color.Red, Color.Green, Color.Yellow, Color.Cyan, Color.Magenta, Color.Blue, Color.White, Color.Gray, Color(0xFFFFA500), Color(0xFF4CAF50))
        
        for (i in 0 until totalPlayers) {
            val army = Army(i, if(i==0) "آپ" else "دشمن ${i}", if (i == 0) playerColor else colors[i % colors.size], i == 0)
            repeat(5) { 
                army.outposts.add(Outpost(
                    id = (i * 1000) + it,
                    ownerId = i,
                    units = UnitCounts((10..15).random(), (10..15).random(), (10..15).random()),
                    posX = (200..1500).random().toFloat(),
                    posY = (200..2500).random().toFloat()
                ))
            }
            list.add(army)
        }
        _armies.value = list
    }

    // باری کے شروع میں یونٹس دینا
    fun getDeployableAmount(armyId: Int): Int {
        val army = _armies.value.find { it.id == armyId }
        return (army?.outposts?.size ?: 0) * 5
    }

    fun deployUnits(postId: Int, armyId: Int) {
        _armies.value = _armies.value.map { army ->
            if (army.id == armyId) {
                army.copy(outposts = army.outposts.map { post ->
                    if (post.id == postId) {
                        post.copy(units = post.units.copy(
                            rocks = post.units.rocks + 2,
                            papers = post.units.papers + 2,
                            scissors = post.units.scissors + 1
                        ))
                    } else post
                }.toMutableList())
            } else army
        }
    }

    fun endTurn() {
        _currentTurnId.value = (_currentTurnId.value + 1) % _armies.value.size
    }
}
