package com.example.model

enum class CardRarity(val label: String, val colorHex: Long) {
    COMMON("Common", 0xFF9E9E9E),
    RARE("Rare", 0xFFFF9800),
    EPIC("Epic", 0xFF9C27B0),
    LEGENDARY("Legendary", 0xFF00E5FF),
    CHAMPION("Champion", 0xFFFFD700)
}

enum class CardType(val label: String) {
    TROOP("Troop"),
    SPELL("Spell"),
    BUILDING("Building")
}

data class ClashCard(
    val id: String,
    val name: String,
    val cost: Int,
    val type: CardType,
    val rarity: CardRarity,
    val emoji: String,
    val isWinCondition: Boolean = false,
    val description: String = ""
)

object ClashCardDatabase {
    val allCards: List<ClashCard> = listOf(
        // 1 Elixir
        ClashCard("skeletons", "Skeletons", 1, CardType.TROOP, CardRarity.COMMON, "💀", false, "Fast cycle distraction units"),
        ClashCard("ice_spirit", "Ice Spirit", 1, CardType.TROOP, CardRarity.COMMON, "❄️", false, "Freezes target for 1.2s"),
        ClashCard("electro_spirit", "Electro Spirit", 1, CardType.TROOP, CardRarity.COMMON, "⚡", false, "Chains up to 9 targets"),
        ClashCard("fire_spirit", "Fire Spirit", 1, CardType.TROOP, CardRarity.COMMON, "🔥", false, "High splash burst damage"),

        // 2 Elixir
        ClashCard("the_log", "The Log", 2, CardType.SPELL, CardRarity.LEGENDARY, "🪵", false, "Knocks back ground swarms"),
        ClashCard("zap", "Zap", 2, CardType.SPELL, CardRarity.COMMON, "⚡", false, "Instant 0.5s stun and reset"),
        ClashCard("ice_golem", "Ice Golem", 2, CardType.TROOP, CardRarity.RARE, "🧊", false, "Cheap kite tank with death slow"),
        ClashCard("bats", "Bats", 2, CardType.TROOP, CardRarity.COMMON, "🦇", false, "5 fast flying swarm units"),
        ClashCard("goblins", "Goblins", 2, CardType.TROOP, CardRarity.COMMON, "🗡️", false, "High DPS ground swarm"),
        ClashCard("spear_goblins", "Spear Goblins", 2, CardType.TROOP, CardRarity.COMMON, "🎯", false, "Ranged chip air/ground"),
        ClashCard("wall_breakers", "Wall Breakers", 2, CardType.TROOP, CardRarity.EPIC, "💣", true, "Fast dual tower demolition"),

        // 3 Elixir
        ClashCard("arrows", "Arrows", 3, CardType.SPELL, CardRarity.COMMON, "🏹", false, "Triple wave swarm clearer"),
        ClashCard("knight", "Knight", 3, CardType.TROOP, CardRarity.COMMON, "⚔️", false, "Reliable mini tank defender"),
        ClashCard("archers", "Archers", 3, CardType.TROOP, CardRarity.COMMON, "🏹", false, "Consistent ranged split defense"),
        ClashCard("skeleton_army", "Skeleton Army", 3, CardType.TROOP, CardRarity.EPIC, "☠️", false, "Overwhelming 15-unit swarm"),
        ClashCard("goblin_barrel", "Goblin Barrel", 3, CardType.SPELL, CardRarity.EPIC, "🛢️", true, "Direct tower bait assault"),
        ClashCard("cannon", "Cannon", 3, CardType.BUILDING, CardRarity.COMMON, "🛡️", false, "Efficient ground building defense"),
        ClashCard("tornado", "Tornado", 3, CardType.SPELL, CardRarity.EPIC, "🌪️", false, "Pulls units & activates King Tower"),
        ClashCard("minions", "Minions", 3, CardType.TROOP, CardRarity.COMMON, "🪽", false, "3 flying air defenders"),
        ClashCard("bandit", "Bandit", 3, CardType.TROOP, CardRarity.LEGENDARY, "🥷", false, "Invulnerable dash attacker"),
        ClashCard("miner", "Miner", 3, CardType.TROOP, CardRarity.LEGENDARY, "⛏️", true, "Burrows anywhere on the arena"),

        // 4 Elixir
        ClashCard("hog_rider", "Hog Rider", 4, CardType.TROOP, CardRarity.RARE, "🐗", true, "Fast bridge jumper tower attacker"),
        ClashCard("fireball", "Fireball", 4, CardType.SPELL, CardRarity.RARE, "☄️", false, "High burst damage with pushback"),
        ClashCard("valkyrie", "Valkyrie", 4, CardType.TROOP, CardRarity.RARE, "🪓", false, "360-degree area whirlwind melee"),
        ClashCard("musketeer", "Musketeer", 4, CardType.TROOP, CardRarity.RARE, "🔫", false, "High single-target sniper"),
        ClashCard("baby_dragon", "Baby Dragon", 4, CardType.TROOP, CardRarity.EPIC, "🐲", false, "Flying splash area attacker"),
        ClashCard("mini_pekka", "Mini P.E.K.K.A", 4, CardType.TROOP, CardRarity.RARE, "🥞", false, "Deadly single-hit tank buster"),
        ClashCard("tesla", "Tesla", 4, CardType.BUILDING, CardRarity.COMMON, "⚡", false, "Hidden air/ground defense"),
        ClashCard("poison", "Poison", 4, CardType.SPELL, CardRarity.EPIC, "🧪", false, "Lingering damage-over-time area"),
        ClashCard("electro_wizard", "Electro Wizard", 4, CardType.TROOP, CardRarity.LEGENDARY, "⚡", false, "Double stun spawn strike"),

        // 5 Elixir
        ClashCard("balloon", "Balloon", 5, CardType.TROOP, CardRarity.EPIC, "🎈", true, "Devastating aerial tower destroyer"),
        ClashCard("witch", "Witch", 5, CardType.TROOP, CardRarity.EPIC, "🧙‍♀️", false, "Summons skeletons with area blasts"),
        ClashCard("wizard", "Wizard", 5, CardType.TROOP, CardRarity.RARE, "🧙‍♂️", false, "Massive fiery area splash damage"),
        ClashCard("inferno_tower", "Inferno Tower", 5, CardType.BUILDING, CardRarity.RARE, "🔥", false, "Melts heaviest tanks over time"),
        ClashCard("bowler", "Bowler", 5, CardType.TROOP, CardRarity.EPIC, "🎳", false, "Boulders roll back ground troops"),
        ClashCard("royal_hogs", "Royal Hogs", 5, CardType.TROOP, CardRarity.RARE, "🐷", true, "Split-lane jumping swarm"),

        // 6 Elixir
        ClashCard("elite_barbarians", "Elite Barbarians", 6, CardType.TROOP, CardRarity.COMMON, "⚔️", true, "Twin ultra-fast rush killers"),
        ClashCard("sparky", "Sparky", 6, CardType.TROOP, CardRarity.LEGENDARY, "🔋", false, "Devastating overcharged doom blast"),
        ClashCard("rocket", "Rocket", 6, CardType.SPELL, CardRarity.RARE, "🚀", false, "Maximum destructive area spell"),
        ClashCard("lightning", "Lightning", 6, CardType.SPELL, CardRarity.EPIC, "🌩️", false, "Strikes highest HP 3 units + stun"),
        ClashCard("elixir_collector", "Elixir Collector", 6, CardType.BUILDING, CardRarity.RARE, "🧪", false, "Pumps out +8 elixir over time"),

        // 7 Elixir
        ClashCard("pekka", "P.E.K.K.A", 7, CardType.TROOP, CardRarity.EPIC, "🤖", false, "Armored super heavy powerhouse"),
        ClashCard("mega_knight", "Mega Knight", 7, CardType.TROOP, CardRarity.LEGENDARY, "🛡️", false, "Crushing drop spawn & jump splash"),
        ClashCard("electro_giant", "Electro Giant", 7, CardType.TROOP, CardRarity.EPIC, "⚡", true, "Reflects damage back to attackers"),
        ClashCard("lava_hound", "Lava Hound", 7, CardType.TROOP, CardRarity.LEGENDARY, "🌋", true, "Flying tank that bursts into pups"),

        // 8 & 9 Elixir
        ClashCard("golem", "Golem", 8, CardType.TROOP, CardRarity.EPIC, "🪨", true, "Colossal tank with dual death golemites"),
        ClashCard("three_musketeers", "Three Musketeers", 9, CardType.TROOP, CardRarity.RARE, "🔫", true, "Trio of musketeers for split pushes")
    )

    fun getCardById(id: String): ClashCard? = allCards.find { it.id == id }

    fun searchCards(query: String): List<ClashCard> {
        if (query.isBlank()) return allCards
        return allCards.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.cost.toString() == query.trim() ||
            it.type.label.contains(query, ignoreCase = true)
        }
    }
}
