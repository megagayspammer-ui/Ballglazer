package com.example.model

enum class CardRarity(val label: String, val colorHex: Long) {
    COMMON("Common", 0xFF94A3B8),
    RARE("Rare", 0xFFF59E0B),
    EPIC("Epic", 0xFFA855F7),
    LEGENDARY("Legendary", 0xFF06B6D4),
    CHAMPION("Champion", 0xFFEAB308)
}

enum class CardRole(val label: String) {
    ALL("All"),
    WIN_CONDITION("Win Condition"),
    TANK("Tank / Mini-Tank"),
    SWARM("Swarm"),
    SUPPORT("Support / DPS"),
    SPELL("Spell"),
    BUILDING("Building")
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
    val role: CardRole,
    val rarity: CardRarity,
    val emoji: String,
    val isWinCondition: Boolean = false,
    val counters: String = "",
    val positiveTradeTip: String = "",
    val description: String = ""
)

object ClashCardDatabase {
    val allCards: List<ClashCard> = listOf(
        // 1 Elixir
        ClashCard(
            id = "skeletons",
            name = "Skeletons",
            cost = 1,
            type = CardType.TROOP,
            role = CardRole.SWARM,
            rarity = CardRarity.COMMON,
            emoji = "💀",
            isWinCondition = false,
            counters = "P.E.K.K.A, Mini P.E.K.K.A, Prince",
            positiveTradeTip = "+6 trade vs P.E.K.K.A with King Tower assist",
            description = "Fast cycle distraction units; surrounds single-target heavy hitters."
        ),
        ClashCard(
            id = "ice_spirit",
            name = "Ice Spirit",
            cost = 1,
            type = CardType.TROOP,
            role = CardRole.SUPPORT,
            rarity = CardRarity.COMMON,
            emoji = "❄️",
            isWinCondition = false,
            counters = "Bats, Minions, Balloon",
            positiveTradeTip = "+4 trade vs Balloon when paired with tower shots",
            description = "Freezes target area for 1.2s; resets Sparky and Inferno Tower."
        ),
        ClashCard(
            id = "electro_spirit",
            name = "Electro Spirit",
            cost = 1,
            type = CardType.TROOP,
            role = CardRole.SUPPORT,
            rarity = CardRarity.COMMON,
            emoji = "⚡",
            isWinCondition = false,
            counters = "Skeleton Army, Bats, Spear Goblins",
            positiveTradeTip = "+2 trade vs Skeleton Army",
            description = "Chains up to 9 targets; stuns and resets attack charge."
        ),
        ClashCard(
            id = "fire_spirit",
            name = "Fire Spirit",
            cost = 1,
            type = CardType.TROOP,
            role = CardRole.SUPPORT,
            rarity = CardRarity.COMMON,
            emoji = "🔥",
            isWinCondition = false,
            counters = "Goblin Barrel, Skeleton Army, Minions",
            positiveTradeTip = "+2 trade vs Goblin Barrel with center jump timing",
            description = "High splash burst damage kamikaze jumper."
        ),

        // 2 Elixir
        ClashCard(
            id = "the_log",
            name = "The Log",
            cost = 2,
            type = CardType.SPELL,
            role = CardRole.SPELL,
            rarity = CardRarity.LEGENDARY,
            emoji = "🪵",
            isWinCondition = false,
            counters = "Goblin Barrel, Princess, Dart Goblin, Tombstone",
            positiveTradeTip = "+1 trade vs Goblin Barrel / Princess",
            description = "Knocks back ground swarms and damages crown towers."
        ),
        ClashCard(
            id = "zap",
            name = "Zap",
            cost = 2,
            type = CardType.SPELL,
            role = CardRole.SPELL,
            rarity = CardRarity.COMMON,
            emoji = "⚡",
            isWinCondition = false,
            counters = "Bats, Skeleton Army, Inferno Dragon",
            positiveTradeTip = "+1 trade vs Skeleton Army; resets Inferno ramp",
            description = "Instant 0.5s stun; resets targeting and charges."
        ),
        ClashCard(
            id = "ice_golem",
            name = "Ice Golem",
            cost = 2,
            type = CardType.TROOP,
            role = CardRole.TANK,
            rarity = CardRarity.RARE,
            emoji = "🧊",
            isWinCondition = false,
            counters = "P.E.K.K.A, Mega Knight, Elite Barbarians",
            positiveTradeTip = "+5 trade by kiting P.E.K.K.A across the arena river",
            description = "Cheap kite tank with death frost nova that kills skeletons."
        ),
        ClashCard(
            id = "bats",
            name = "Bats",
            cost = 2,
            type = CardType.TROOP,
            role = CardRole.SWARM,
            rarity = CardRarity.COMMON,
            emoji = "🦇",
            isWinCondition = false,
            counters = "Valkyrie, Mega Knight, Balloon, Knight",
            positiveTradeTip = "+5 trade vs Mega Knight if undefended by splash",
            description = "5 flying swarm units with lethal single-target DPS."
        ),
        ClashCard(
            id = "goblins",
            name = "Goblins",
            cost = 2,
            type = CardType.TROOP,
            role = CardRole.SWARM,
            rarity = CardRarity.COMMON,
            emoji = "🗡️",
            isWinCondition = false,
            counters = "Hog Rider, Miner, Mini P.E.K.K.A",
            positiveTradeTip = "+2 trade vs Hog Rider (allows 0-1 hit)",
            description = "High ground DPS swarm with very fast movement."
        ),
        ClashCard(
            id = "spear_goblins",
            name = "Spear Goblins",
            cost = 2,
            type = CardType.TROOP,
            role = CardRole.SUPPORT,
            rarity = CardRarity.COMMON,
            emoji = "🎯",
            isWinCondition = false,
            counters = "Bats, Minions, Inferno Dragon",
            positiveTradeTip = "+2 trade vs Inferno Dragon with tower help",
            description = "Ranged chip air/ground trio for defensive spacing."
        ),
        ClashCard(
            id = "wall_breakers",
            name = "Wall Breakers",
            cost = 2,
            type = CardType.TROOP,
            role = CardRole.WIN_CONDITION,
            rarity = CardRarity.EPIC,
            emoji = "💣",
            isWinCondition = true,
            counters = "Buildings, Crown Towers",
            positiveTradeTip = "Forces opponent to spend 3-4 elixir to defend 2 elixir",
            description = "Dual suicide bomb runners; immense split-lane pressure."
        ),

        // 3 Elixir
        ClashCard(
            id = "arrows",
            name = "Arrows",
            cost = 3,
            type = CardType.SPELL,
            role = CardRole.SPELL,
            rarity = CardRarity.COMMON,
            emoji = "🏹",
            isWinCondition = false,
            counters = "Minion Horde, Goblin Barrel, Firecracker, Archers",
            positiveTradeTip = "+2 trade vs Minion Horde",
            description = "Triple wave wide-area swarm clearer; eliminates Firecracker."
        ),
        ClashCard(
            id = "knight",
            name = "Knight",
            cost = 3,
            type = CardType.TROOP,
            role = CardRole.TANK,
            rarity = CardRarity.COMMON,
            emoji = "⚔️",
            isWinCondition = false,
            counters = "Mega Knight, Wizard, Witch, Musketeer",
            positiveTradeTip = "+4 trade vs Mega Knight with tower support",
            description = "Staple mini-tank with exceptional HP-to-cost ratio."
        ),
        ClashCard(
            id = "archers",
            name = "Archers",
            cost = 3,
            type = CardType.TROOP,
            role = CardRole.SUPPORT,
            rarity = CardRarity.COMMON,
            emoji = "🏹",
            isWinCondition = false,
            counters = "Graveyard, Minions, Balloon",
            positiveTradeTip = "+2 trade vs Graveyard defense",
            description = "Consistent ranged split defense; survives The Log and Zap."
        ),
        ClashCard(
            id = "skeleton_army",
            name = "Skeleton Army",
            cost = 3,
            type = CardType.TROOP,
            role = CardRole.SWARM,
            rarity = CardRarity.EPIC,
            emoji = "☠️",
            isWinCondition = false,
            counters = "Prince, P.E.K.K.A, Giant, Hog Rider",
            positiveTradeTip = "+4 trade vs P.E.K.K.A or Prince",
            description = "Overwhelming 15-unit swarm; instant tank shredder."
        ),
        ClashCard(
            id = "goblin_barrel",
            name = "Goblin Barrel",
            cost = 3,
            type = CardType.SPELL,
            role = CardRole.WIN_CONDITION,
            rarity = CardRarity.EPIC,
            emoji = "🛢️",
            isWinCondition = true,
            counters = "Crown Towers (Bait Win Condition)",
            positiveTradeTip = "Triggers Log Bait archetypes",
            description = "Direct tower bait assault; 3 goblins surround the princess tower."
        ),
        ClashCard(
            id = "cannon",
            name = "Cannon",
            cost = 3,
            type = CardType.BUILDING,
            role = CardRole.BUILDING,
            rarity = CardRarity.COMMON,
            emoji = "🛡️",
            isWinCondition = false,
            counters = "Hog Rider, Giant, Ram Rider, Battle Ram",
            positiveTradeTip = "+1 trade vs Hog Rider for 0 tower damage",
            description = "Efficient ground building defense; 2.6 Hog cycle cornerstone."
        ),
        ClashCard(
            id = "tornado",
            name = "Tornado",
            cost = 3,
            type = CardType.SPELL,
            role = CardRole.SPELL,
            rarity = CardRarity.EPIC,
            emoji = "🌪️",
            isWinCondition = false,
            counters = "Hog Rider, Goblin Barrel, Miner, Balloon",
            positiveTradeTip = "Activates King Tower early for permanent 3v2 defense",
            description = "Pulls units into center; combos with splash damage."
        ),
        ClashCard(
            id = "bandit",
            name = "Bandit",
            cost = 3,
            type = CardType.TROOP,
            role = CardRole.SUPPORT,
            rarity = CardRarity.LEGENDARY,
            emoji = "🥷",
            isWinCondition = false,
            counters = "Princess, Dart Goblin, Musketeer, Wizard",
            positiveTradeTip = "+2 trade vs Wizard with dash immunity",
            description = "Invulnerable dash attack dealing double strike damage."
        ),
        ClashCard(
            id = "miner",
            name = "Miner",
            cost = 3,
            type = CardType.TROOP,
            role = CardRole.WIN_CONDITION,
            rarity = CardRarity.LEGENDARY,
            emoji = "⛏️",
            isWinCondition = true,
            counters = "Princess, Elixir Collector, Dart Goblin",
            positiveTradeTip = "+3 trade vs Elixir Collector",
            description = "Burrows underground directly to any arena tile."
        ),

        // 4 Elixir
        ClashCard(
            id = "hog_rider",
            name = "Hog Rider",
            cost = 4,
            type = CardType.TROOP,
            role = CardRole.WIN_CONDITION,
            rarity = CardRarity.RARE,
            emoji = "🐗",
            isWinCondition = true,
            counters = "Crown Towers, Defensive Buildings",
            positiveTradeTip = "Punish play when opponent drops Golem/PEKKA",
            description = "Fast bridge jumping building-targeting striker."
        ),
        ClashCard(
            id = "fireball",
            name = "Fireball",
            cost = 4,
            type = CardType.SPELL,
            role = CardRole.SPELL,
            rarity = CardRarity.RARE,
            emoji = "☄️",
            isWinCondition = false,
            counters = "Musketeer, Wizard, Electro Wizard, Flying Machine",
            positiveTradeTip = "+1 trade vs Wizard / Witch plus tower chip",
            description = "High burst damage with strong physics pushback."
        ),
        ClashCard(
            id = "valkyrie",
            name = "Valkyrie",
            cost = 4,
            type = CardType.TROOP,
            role = CardRole.TANK,
            rarity = CardRarity.RARE,
            emoji = "🪓",
            isWinCondition = false,
            counters = "Skeleton Army, Witch, Goblin Gang, Barbarians",
            positiveTradeTip = "+1 trade vs Witch, kills spawned skeletons simultaneously",
            description = "360-degree area whirlwind melee; great backline assassin."
        ),
        ClashCard(
            id = "musketeer",
            name = "Musketeer",
            cost = 4,
            type = CardType.TROOP,
            role = CardRole.SUPPORT,
            rarity = CardRarity.RARE,
            emoji = "🔫",
            isWinCondition = false,
            counters = "Baby Dragon, Balloon, Inferno Dragon, Mega Minion",
            positiveTradeTip = "+1 trade vs Balloon without letting it drop a bomb",
            description = "High single-target sniper with long 6.0 tile range."
        ),
        ClashCard(
            id = "mini_pekka",
            name = "Mini P.E.K.K.A",
            cost = 4,
            type = CardType.TROOP,
            role = CardRole.SUPPORT,
            rarity = CardRarity.RARE,
            emoji = "🥞",
            isWinCondition = false,
            counters = "Hog Rider, Giant, Royal Giant, Golem",
            positiveTradeTip = "+2 trade vs Giant, eliminates tank in 3 strikes",
            description = "Deadly single-hit tank buster; hits like a freight train."
        ),
        ClashCard(
            id = "tesla",
            name = "Tesla",
            cost = 4,
            type = CardType.BUILDING,
            role = CardRole.BUILDING,
            rarity = CardRarity.COMMON,
            emoji = "⚡",
            isWinCondition = false,
            counters = "Balloon, Hog Rider, Lava Hound, Baby Dragon",
            positiveTradeTip = "+1 trade vs Balloon; immune to spells when underground",
            description = "Hidden air/ground defense; burrows underground when idle."
        ),
        ClashCard(
            id = "poison",
            name = "Poison",
            cost = 4,
            type = CardType.SPELL,
            role = CardRole.SPELL,
            rarity = CardRarity.EPIC,
            emoji = "🧪",
            isWinCondition = false,
            counters = "Graveyard, Night Witch, Witch, Musketeer",
            positiveTradeTip = "+1 trade vs Graveyard, completely negates skeletons",
            description = "Lingering damage-over-time area; shuts down swarm pushes."
        ),
        ClashCard(
            id = "electro_wizard",
            name = "Electro Wizard",
            cost = 4,
            type = CardType.TROOP,
            role = CardRole.SUPPORT,
            rarity = CardRarity.LEGENDARY,
            emoji = "⚡",
            isWinCondition = false,
            counters = "Sparky, Inferno Dragon, Prince, Ram Rider",
            positiveTradeTip = "+2 trade vs Sparky by continuously resetting charge",
            description = "Double stun spawn strike; perma-stuns charge units."
        ),

        // 5 Elixir
        ClashCard(
            id = "balloon",
            name = "Balloon",
            cost = 5,
            type = CardType.TROOP,
            role = CardRole.WIN_CONDITION,
            rarity = CardRarity.EPIC,
            emoji = "🎈",
            isWinCondition = true,
            counters = "Crown Towers",
            positiveTradeTip = "Devastating win condition; pair with Lumberjack / Freeze",
            description = "Devastating aerial tower destroyer with heavy death bomb."
        ),
        ClashCard(
            id = "witch",
            name = "Witch",
            cost = 5,
            type = CardType.TROOP,
            role = CardRole.SUPPORT,
            rarity = CardRarity.EPIC,
            emoji = "🧙‍♀️",
            isWinCondition = false,
            counters = "P.E.K.K.A, Prince, Mini P.E.K.K.A",
            positiveTradeTip = "+2 trade vs P.E.K.K.A with distraction skeletons",
            description = "Summons 4 skeletons every 7s with ranged splash blasts."
        ),
        ClashCard(
            id = "wizard",
            name = "Wizard",
            cost = 5,
            type = CardType.TROOP,
            role = CardRole.SUPPORT,
            rarity = CardRarity.RARE,
            emoji = "🧙‍♂️",
            isWinCondition = false,
            counters = "Minion Horde, Skeleton Army, Barbarians",
            positiveTradeTip = "Hard counter to swarms; vulnerable to Fireball + Log",
            description = "Massive fiery area splash damage on air and ground."
        ),
        ClashCard(
            id = "inferno_tower",
            name = "Inferno Tower",
            cost = 5,
            type = CardType.BUILDING,
            role = CardRole.BUILDING,
            rarity = CardRarity.RARE,
            emoji = "🔥",
            isWinCondition = false,
            counters = "Golem, P.E.K.K.A, Mega Knight, Lava Hound, Electro Giant",
            positiveTradeTip = "+3 trade vs Golem, +2 trade vs P.E.K.K.A",
            description = "Beams ramp up damage to melt even the heaviest tanks."
        ),

        // 6 Elixir
        ClashCard(
            id = "elite_barbarians",
            name = "Elite Barbarians",
            cost = 6,
            type = CardType.TROOP,
            role = CardRole.WIN_CONDITION,
            rarity = CardRarity.COMMON,
            emoji = "⚔️",
            isWinCondition = true,
            counters = "Golem, Royal Giant, Hog Rider",
            positiveTradeTip = "+2 trade vs Golem defensive shred",
            description = "Twin ultra-fast rush killers with massive DPS."
        ),
        ClashCard(
            id = "sparky",
            name = "Sparky",
            cost = 6,
            type = CardType.TROOP,
            role = CardRole.SUPPORT,
            rarity = CardRarity.LEGENDARY,
            emoji = "🔋",
            isWinCondition = false,
            counters = "Golem, P.E.K.K.A, Mega Knight, Giant",
            positiveTradeTip = "Overkill doom blast; reset with Zap/Electro Spirit",
            description = "Devastating overcharged splash doom blast (1300+ damage)."
        ),
        ClashCard(
            id = "rocket",
            name = "Rocket",
            cost = 6,
            type = CardType.SPELL,
            role = CardRole.SPELL,
            rarity = CardRarity.RARE,
            emoji = "🚀",
            isWinCondition = false,
            counters = "Sparky, Elixir Collector, X-Bow, Three Musketeers",
            positiveTradeTip = "+3 trade vs Three Musketeers",
            description = "Maximum destructive area spell; guaranteed tower finisher."
        ),
        ClashCard(
            id = "lightning",
            name = "Lightning",
            cost = 6,
            type = CardType.SPELL,
            role = CardRole.SPELL,
            rarity = CardRarity.EPIC,
            emoji = "🌩️",
            isWinCondition = false,
            counters = "Inferno Tower, Musketeer, Wizard, Witch",
            positiveTradeTip = "+2 to +4 trade when hitting building + support troop",
            description = "Strikes highest HP 3 units + 0.5s stun across 3.5 radius."
        ),

        // 7 Elixir
        ClashCard(
            id = "pekka",
            name = "P.E.K.K.A",
            cost = 7,
            type = CardType.TROOP,
            role = CardRole.TANK,
            rarity = CardRarity.EPIC,
            emoji = "🤖",
            isWinCondition = false,
            counters = "Mega Knight, Golem, Giant, Elite Barbarians",
            positiveTradeTip = "+1 trade vs Golem + counters full HP on push",
            description = "Armored super heavy powerhouse; absolute tank annihilator."
        ),
        ClashCard(
            id = "mega_knight",
            name = "Mega Knight",
            cost = 7,
            type = CardType.TROOP,
            role = CardRole.TANK,
            rarity = CardRarity.LEGENDARY,
            emoji = "🛡️",
            isWinCondition = false,
            counters = "Barbarians, Elite Barbarians, Royal Hogs, Witch",
            positiveTradeTip = "Huge swing on drop; counter with Knight or Valkyrie in center",
            description = "Crushing drop spawn & jump splash; stomps ground clusters."
        ),
        ClashCard(
            id = "electro_giant",
            name = "Electro Giant",
            cost = 7,
            type = CardType.TROOP,
            role = CardRole.WIN_CONDITION,
            rarity = CardRarity.EPIC,
            emoji = "⚡",
            isWinCondition = true,
            counters = "Crown Towers, Defensive Swarms",
            positiveTradeTip = "Zap coil reflects damage back to swarms; counter with Inferno/Mini PEKKA",
            description = "Reflects damage back to attackers with stun zap coils."
        ),

        // 8 & 9 Elixir
        ClashCard(
            id = "golem",
            name = "Golem",
            cost = 8,
            type = CardType.TROOP,
            role = CardRole.WIN_CONDITION,
            rarity = CardRarity.EPIC,
            emoji = "🪨",
            isWinCondition = true,
            counters = "Crown Towers",
            positiveTradeTip = "Heavy investment; punish opposite lane immediately with Hog/Bandit",
            description = "Colossal tank; bursts into dual death golemites with area push."
        ),
        ClashCard(
            id = "three_musketeers",
            name = "Three Musketeers",
            cost = 9,
            type = CardType.TROOP,
            role = CardRole.WIN_CONDITION,
            rarity = CardRarity.RARE,
            emoji = "🔫",
            isWinCondition = true,
            counters = "Tanks, Balloon, Lava Hound",
            positiveTradeTip = "Vulnerable to Fireball / Poison / Rocket for massive positive trades",
            description = "Trio of musketeers; split behind King Tower for dual-lane assault."
        )
    )

    fun getCardById(id: String): ClashCard? = allCards.find { it.id == id }

    fun searchCards(query: String, role: CardRole = CardRole.ALL, costFilter: Int? = null): List<ClashCard> {
        return allCards.filter { card ->
            val matchesCost = costFilter == null || card.cost == costFilter
            val matchesRole = role == CardRole.ALL || card.role == role
            val matchesQuery = query.isBlank() ||
                    card.name.contains(query, ignoreCase = true) ||
                    card.cost.toString() == query.trim() ||
                    card.type.label.contains(query, ignoreCase = true)
            matchesCost && matchesRole && matchesQuery
        }
    }
}
