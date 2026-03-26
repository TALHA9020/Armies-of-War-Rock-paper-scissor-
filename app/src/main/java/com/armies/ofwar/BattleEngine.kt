// BattleEngine.kt میں درج ذیل فنکشنز اور لاجک شامل کریں

// دشمن کی باری کی لاجک
fun startEnemyTurn() {
    engineScope.launch {
        delay(1500) // دشمن کے سوچنے کا وقت
        val enemyArmy = _armies.value.find { it.id == _currentTurnId.value }
        
        if (enemyArmy != null && !enemyArmy.isUserControlled) {
            // دشمن کی ایک رینڈم چوکی کا انتخاب
            val attackerOutpost = enemyArmy.outposts.randomOrNull()
            // پلیئر کی ایک چوکی کو ٹارگٹ بنانا
            val targetOutpost = _armies.value.find { it.isUserControlled }?.outposts?.randomOrNull()

            if (attackerOutpost != null && targetOutpost != null) {
                // دشمن رینڈملی R, P, یا S لہر بھیجے گا
                val unitTypes = listOf(UnitType.ROCK, UnitType.PAPER, UnitType.SCISSORS)
                executeBattle(attackerOutpost, targetOutpost, unitTypes.random())
            }
        }
        delay(1000)
        endTurn()
    }
}

// جنگ اور غلبہ کے قوانین (RPS Logic)
fun executeBattle(attacker: Outpost, defender: Outpost, attackType: UnitType) {
    val defenderArmy = _armies.value.find { it.id == defender.ownerId }
    // دفاعی یونٹ کا انتخاب (دشمن یا پلیئر خودکار طور پر بہترین یونٹ سے دفاع کرے گا)
    val defenseType = listOf(UnitType.ROCK, UnitType.PAPER, UnitType.SCISSORS).random()

    val attackWon = RPSRules.resolve(attackType, defenseType)

    _armies.value = _armies.value.map { army ->
        army.copy(outposts = army.outposts.map { outpost ->
            if (outpost.id == defender.id) {
                val newUnits = outpost.units.copy()
                if (attackWon == true) {
                    // حملہ آور جیتا: دفاع کرنے والے کے یونٹس کم ہوں گے
                    when(defenseType) {
                        UnitType.ROCK -> newUnits.rocks = (newUnits.rocks - 5).coerceAtLeast(0)
                        UnitType.PAPER -> newUnits.papers = (newUnits.papers - 5).coerceAtLeast(0)
                        UnitType.SCISSORS -> newUnits.scissors = (newUnits.scissors - 5).coerceAtLeast(0)
                        else -> {}
                    }
                } else if (attackWon == false || attackType == defenseType) {
                    // دفاع کرنے والا جیتا یا سیم یونٹس (قانون کے مطابق دفاعی حاوی ہے)
                    // یہاں حملہ آور کی لہر ضائع ہو جائے گی اور دفاعی چوکی محفوظ رہے گی
                }
                outpost.copy(units = newUnits)
            } else outpost
        }.toMutableList())
    }
}
