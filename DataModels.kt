import java.io.Serializable

// Enum representing continents
enum class Continent(val displayName: String) : Serializable {
    ASIA("Asia"),
    AFRICA("Africa"),
    EUROPE("Europe"),
    NORTH_AMERICA("North America"),
    SOUTH_AMERICA("South America"),
    AUSTRALIA("Australia")
}

// Enum representing RPS choices
enum class RPSChoice : Serializable {
    ROCK,
    PAPER,
    SCISSORS
}

// Enum representing game phases
enum class GamePhase : Serializable {
    SETUP,
    IN_PROGRESS,
    FINISHED
}

// Data class representing a round of Rock-Paper-Scissors
data class RPSRound(
    val playerChoice: RPSChoice,
    val opponentChoice: RPSChoice,
    val winner: String?
) : Serializable

// Data class representing the result of a battle
data class BattleResult(
    val battleWinner: String,
    val tieReason: String?
) : Serializable

// Data class representing a territory
data class Territory(
    val id: Int,
    val name: String,
    val ownerId: String,
    val troops: Int,
    val continent: Continent,
    val posX: Int,
    val posY: Int,
    val adjacentTerritories: List<Int>
) : Serializable

// Data class representing a player
data class Player(
    val id: String,
    val name: String,
    val color: String,
    val isHuman: Boolean,
    val troops: Int,
    val cards: List<String>,
    val territories: List<Territory>,
    val isAlive: Boolean,
    val hasConquered: Boolean
) : Serializable

// Data class representing the game state
data class GameState(
    val currentPlayerId: String,
    val gamePhase: GamePhase,
    val totalPlayers: Int,
    val players: List<Player>,
    val allTerritories: List<Territory>,
    val turn: Int
) : Serializable