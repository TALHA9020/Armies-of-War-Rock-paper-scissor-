package com.armies.ofwar

import java.io.Serializable

enum class Continent : Serializable {
    AFRICA,
    ASIA,
    EUROPE,
    NORTH_AMERICA,
    SOUTH_AMERICA,
    AUSTRALIA,
    ANTARCTICA
}

enum class RPSChoice : Serializable {
    ROCK,
    PAPER,
    SCISSORS
}

enum class GamePhase : Serializable {
    READY,
    PLAYING,
    FINISHED
}

data class Territory(val name: String, val continent: Continent) : Serializable

data class Player(val name: String, val territory: Territory) : Serializable

data class GameState(val players: List<Player>, val currentPhase: GamePhase) : Serializable

data class RPSRound(val player1Choice: RPSChoice, val player2Choice: RPSChoice) : Serializable

data class BattleResult(val winner: Player, val round: RPSRound) : Serializable