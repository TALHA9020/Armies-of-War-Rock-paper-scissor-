package com.armies.ofwar

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BattleEngine {
    private val _armies = MutableStateFlow<List<Army>>(emptyList())
    val armies: StateFlow<List<Army>> = _armies

    private val _currentTurnId = MutableStateFlow(0)
    val currentTurnId: StateFlow<Int> = _currentTurnId

    private val _deployableUnits = MutableStateFlow(0)
    val deployableUnits: StateFlow<Int> = _deployableUnits

    fun setupGame(totalPlayers: Int, playerColor: Color) {
        val list = mutableListOf<Army>()
        val colors = listOf(Color.Cyan, Color.Red, Color.Green, Color.Yellow, Color.Magenta, Color.Blue, Color.White, Color.Gray, Color(0xFFFFA500), Color(0xFF4CAF50))
        
        for (i in 0 until totalPlayers) {
            val army = Army(i, if(i==0) "آپ" else "دشمن $i", if (i == 0) playerColor else colors[i % colors.size], i == 0)
            repeat(10) { 
                army.outposts.add(Outpost(
                    id = (i * 1000) + it,
                    ownerId = i,
                    units = UnitCounts((10..15).random(), (10..15).random(), (10..15).random()),
                    posX = (200..2000).random().toFloat(),
                    posY = (200..3000).random().toFloat()
                ))
            }
            list.add(army)
        }
        _armies.value = list
        updateDeployable()
    }

    private fun updateDeployable() {
        val army = _armies.value.find { it.id == _currentTurnId.value }
        _deployableUnits.value = (army?.outposts?.size ?: 0) * 3
    }

    fun deployAt(postId: Int) {
        if (_deployableUnits.value > 0) {
            _armies.value = _armies.value.map { army ->
                army.copy(outposts = army.outposts.map { post ->
                    if (post.id == postId) {
                        _deployableUnits.value--
                        post.copy(units = post.units.copy(rocks = post.units.rocks + 1))
                    } else post
                }.toMutableList())
            }
        }
    }

    // 9: یونٹس کی منتقلی (Move Units)
    fun moveUnits(fromId: Int, toId: Int) {
        _armies.value = _armies.value.map { army ->
            val fromPost = army.outposts.find { it.id == fromId }
            val toPost = army.outposts.find { it.id == toId }
            if (fromPost != null && toPost != null && fromPost.units.total > 1) {
                army.copy(outposts = army.outposts.map { post ->
                    when (post.id) {
                        fromId -> post.copy(units = post.units.copy(rocks = post.units.rocks - 1))
                        toId -> post.copy(units = post.units.copy(rocks = post.units.rocks + 1))
                        else -> post
                    }
                }.toMutableList())
            } else army
        }
    }

    fun endTurn() {
        _currentTurnId.value = (_currentTurnId.value + 1) % _armies.value.size
        updateDeployable()
    }
}
