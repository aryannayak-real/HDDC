package com.example.util

import com.example.data.model.PlayerDetail

object ClashPlannerData {

    data class UpgradeStep(
        val category: String, // "OFFENSE", "CORE_DEFENSE", "HERO", "LAB", "ECONOMY"
        val name: String,
        val reason: String,
        val priorityTag: String // "Day 1 Mandatory", "High Priority", "Strategic", "Ongoing"
    )

    data class TownHallGuide(
        val thLevel: Int,
        val title: String,
        val strategicSummary: String,
        val steps: List<UpgradeStep>,
        val recommendedArmies: List<String>
    )

    val TOWN_HALL_GUIDES = listOf(
        TownHallGuide(
            thLevel = 17,
            title = "Town Hall 17 — The Celestial Citadel Meta",
            strategicSummary = "TH17 introduces Dark Hero (Minion Prince), level 17 Giga Weapon, and ultra-high defense density. Offense and Hero Equipment dictating 3-star capability are absolute Day 1 priorities.",
            steps = listOf(
                UpgradeStep("OFFENSE", "Laboratory & Clan Castle", "Unlock instant troop boost levels and +5 spell/troop capacity.", "Day 1 Mandatory"),
                UpgradeStep("OFFENSE", "Blacksmith (Level 10)", "Increases Hero Equipment level caps and unlocks Celestial Ores capacity.", "Day 1 Mandatory"),
                UpgradeStep("OFFENSE", "Army Camps (All 4)", "Adds +20 total troop space for new TH17 army compositions.", "High Priority"),
                UpgradeStep("CORE_DEFENSE", "Town Hall Weapon 5★", "Maximizes defensive DPS and post-destruction bomb damage.", "High Priority"),
                UpgradeStep("CORE_DEFENSE", "Monolith & Eagle Artillery", "Key anti-hero and anti-tank anchor defenses.", "High Priority"),
                UpgradeStep("CORE_DEFENSE", "Ricochet Cannons & Multi-Archer Towers", "Fused defenses that melt hybrid and smash attacks.", "Strategic"),
                UpgradeStep("HERO", "Barbarian King (Lv 100) & AQ (Lv 100)", "King tanking and Queen charge damage dictate war consistency.", "Ongoing"),
                UpgradeStep("HERO", "Grand Warden (Lv 75) & RC (Lv 50)", "Warden aura boost and Royal Champion cleanup value.", "Ongoing"),
                UpgradeStep("LAB", "Root Riders, Super Miners & Overgrowth Spell", "Dominant smash meta for 3-star CWL strategies.", "High Priority")
            ),
            recommendedArmies = listOf("Root Rider + Valkyrie Smash", "Super Archer Blimp + Dragons", "Queen Charge Hybrid")
        ),
        TownHallGuide(
            thLevel = 16,
            title = "Town Hall 16 — Merged Defenses & Root Riders",
            strategicSummary = "TH16 shifts the defensive paradigm with merged defenses (Ricochet Cannon & Multi-Archer Tower). Prioritize Blacksmith, Pet House, and merged defenses.",
            steps = listOf(
                UpgradeStep("OFFENSE", "Laboratory & Clan Castle", "Enables power potion max boosts and siege machine donations.", "Day 1 Mandatory"),
                UpgradeStep("OFFENSE", "Blacksmith (Level 9)", "Maxes Common Equipment to level 18 and Epic Equipment to level 27.", "Day 1 Mandatory"),
                UpgradeStep("OFFENSE", "Pet House (Level 9)", "Unlocks Spirit Fox, the undisputed best pet for Royal Champion.", "Day 1 Mandatory"),
                UpgradeStep("CORE_DEFENSE", "Merge Ricochet Cannons (x2)", "Doubles DPS and shoots bouncing cannonballs that pierce targets.", "High Priority"),
                UpgradeStep("CORE_DEFENSE", "Merge Multi-Archer Towers (x2)", "Rapid-fire volleys that eliminate swarms and hero pushes.", "High Priority"),
                UpgradeStep("CORE_DEFENSE", "Scattershots & Inferno Towers", "Anchor defenses against heavy ground pushes.", "Strategic"),
                UpgradeStep("HERO", "Barbarian King (95), Queen (95), Warden (70), RC (45)", "Essential for breaking maxed bases.", "Ongoing"),
                UpgradeStep("LAB", "Root Rider (Lv 3) & Healers", "Most reliable 3-star attack core.", "High Priority")
            ),
            recommendedArmies = listOf("Root Rider Spam", "Super Bowler Smash", "Zap Lalo")
        ),
        TownHallGuide(
            thLevel = 15,
            title = "Town Hall 15 — The Magic & Monolith Threshold",
            strategicSummary = "TH15 features the Monolith and Spell Towers. The defensive power spike is extreme. Counteract by rushing Army Camps, Clan Castle, and Diggy/Frosty in Pet House.",
            steps = listOf(
                UpgradeStep("OFFENSE", "Laboratory & Clan Castle", "Day 1 must to activate +5 level troop boost.", "Day 1 Mandatory"),
                UpgradeStep("OFFENSE", "Army Camps & Pet House", "Unlocks Diggy (stun pet) and Frosty, plus 320 army space.", "Day 1 Mandatory"),
                UpgradeStep("CORE_DEFENSE", "Build & Max Monolith", "Deals % max HP damage; the ultimate hero killer.", "High Priority"),
                UpgradeStep("CORE_DEFENSE", "Build & Max Spell Towers (x2)", "Rage and Poison spell towers foil blimp attacks.", "High Priority"),
                UpgradeStep("CORE_DEFENSE", "Eagle Artillery & Giga Inferno 5★", "Prevents 3-stars on your own village.", "Strategic"),
                UpgradeStep("HERO", "Heroes to BK 90, AQ 90, GW 65, RC 40", "Required for max hero equipment synergy.", "Ongoing")
            ),
            recommendedArmies = listOf("Super Barch / Blizzard Lalo", "Electro Titan Smash", "Skelly Donut Lalo")
        ),
        TownHallGuide(
            thLevel = 14,
            title = "Town Hall 14 — Jungle Pets & Builder Huts",
            strategicSummary = "Introduces Hero Pets and Weaponized Builder Huts that heal defenses. Max Clan Castle, Laboratory, and Unicorn early.",
            steps = listOf(
                UpgradeStep("OFFENSE", "Laboratory & Clan Castle", "Unlock siege capacity and boost troop power.", "Day 1 Mandatory"),
                UpgradeStep("OFFENSE", "Pet House (Unlock Unicorn)", "Unicorn attaches to Queen for permanent healing.", "Day 1 Mandatory"),
                UpgradeStep("CORE_DEFENSE", "Battle Builder Huts", "Turns builder huts into defensive turrets that repair structures.", "High Priority"),
                UpgradeStep("CORE_DEFENSE", "Eagle Artillery & Scattershots", "High collateral area damage against clumps.", "High Priority"),
                UpgradeStep("HERO", "Heroes to BK 80, AQ 80, GW 55, RC 30", "Unlock high level hero ability potency.", "Ongoing")
            ),
            recommendedArmies = listOf("Super Dragon Smash", "Hydra (Dragons + Dragon Riders)", "Queen Charge Hybrid")
        ),
        TownHallGuide(
            thLevel = 13,
            title = "Town Hall 13 — Royal Champion & Scattershots",
            strategicSummary = "Unlocks the 4th hero (Royal Champion) and Scattershots. Max RC quickly as her Seeking Shield carries lower attacks.",
            steps = listOf(
                UpgradeStep("HERO", "Unlock & Upgrade Royal Champion", "Get RC to level 10+ immediately for war potency.", "Day 1 Mandatory"),
                UpgradeStep("OFFENSE", "Laboratory & Clan Castle (+Siege)", "Max Clan Castle holds 45 troops + 2 spells.", "Day 1 Mandatory"),
                UpgradeStep("CORE_DEFENSE", "Build Both Scattershots", "Crucial anti-swarm defense.", "High Priority"),
                UpgradeStep("CORE_DEFENSE", "Giga Inferno & Eagle Artillery", "Stops mass troop pushes at center.", "Strategic"),
                UpgradeStep("HERO", "Heroes to BK 75, AQ 75, GW 50, RC 25", "High survivability threshold.", "Ongoing")
            ),
            recommendedArmies = listOf("Queen Charge Hybrid (Miners + Hogs)", "Yeti Smash", "Blizzard Lalo")
        ),
        TownHallGuide(
            thLevel = 12,
            title = "Town Hall 12 — Giga Tesla & Siege Workshop",
            strategicSummary = "The first Town Hall with a weaponized Giga Tesla. Unlocks Siege Machines which fundamentally transform all attacks.",
            steps = listOf(
                UpgradeStep("OFFENSE", "Laboratory & Siege Workshop", "Build Workshop to produce Wall Wreckers and Battle Blimps.", "Day 1 Mandatory"),
                UpgradeStep("OFFENSE", "Clan Castle & Army Camps", "Total capacity reaches 280 troops.", "High Priority"),
                UpgradeStep("CORE_DEFENSE", "Giga Tesla to 5★ (Explosion Bomb)", "Level 5 drops a fatal bomb on destruction.", "High Priority"),
                UpgradeStep("CORE_DEFENSE", "Eagle Artillery & Inferno Towers", "Crucial core anchor defense.", "Strategic"),
                UpgradeStep("HERO", "Grand Warden to 40, BK 65, AQ 65", "Eternal Tome invulnerability reaches maximum duration.", "Ongoing")
            ),
            recommendedArmies = listOf("Zap Witches", "Bat Slap (Pekka + BoBat)", "Queen Charge Lalo")
        ),
        TownHallGuide(
            thLevel = 11,
            title = "Town Hall 11 — Grand Warden & Eagle Artillery",
            strategicSummary = "Unlocks the Grand Warden and Eagle Artillery. Eternal Tome renders entire armies invincible during crucial core pushes.",
            steps = listOf(
                UpgradeStep("HERO", "Unlock Grand Warden", "Get Warden to Lv 5 immediately for Eternal Tome ability.", "Day 1 Mandatory"),
                UpgradeStep("OFFENSE", "Laboratory & Clan Castle", "Unlock Electro Dragons and max potion power.", "Day 1 Mandatory"),
                UpgradeStep("CORE_DEFENSE", "Build Eagle Artillery", "Global map artillery defense.", "High Priority"),
                UpgradeStep("HERO", "Heroes to BK 50, AQ 50, GW 20", "Max out Queen Charge strength.", "Ongoing")
            ),
            recommendedArmies = listOf("Zap Witches / 5 Icy G", "Hybrid (Miner + Hog)", "Electro Dragon Funnel")
        )
    )

    // Hero Equipment Metadata & Ore Calculation
    data class EquipmentItem(
        val name: String,
        val hero: String,
        val isEpic: Boolean,
        val maxLevel: Int,
        val description: String
    )

    val HERO_EQUIPMENTS = listOf(
        // Barbarian King
        EquipmentItem("Giant Gauntlet", "Barbarian King", true, 27, "Transforms King into a giant dealing massive splash damage and reducing taken damage."),
        EquipmentItem("Spiky Ball", "Barbarian King", true, 27, "Launches a bouncing projectile damaging multiple distant defenses."),
        EquipmentItem("Rage Vial", "Barbarian King", false, 18, "Boosts King speed and attack damage on ability activation."),
        // Archer Queen
        EquipmentItem("Frozen Arrow", "Archer Queen", true, 27, "Passive slow on targets hit by Queen arrows. Essential for Queen Charge."),
        EquipmentItem("Invisibility Vial", "Archer Queen", false, 18, "Grants temporary invisibility and high burst damage."),
        EquipmentItem("Giant Arrow", "Archer Queen", false, 18, "Pierces across the entire map, snipping air defenses or sweepers."),
        // Grand Warden
        EquipmentItem("Fireball", "Grand Warden", true, 27, "Nuclear splash damage destroying clustered defense compartments."),
        EquipmentItem("Eternal Tome", "Grand Warden", false, 18, "Renders all friendly troops in aura immune to damage for up to 9.5s."),
        EquipmentItem("Healing Tome", "Grand Warden", false, 18, "Heals friendly troops inside Warden aura."),
        // Royal Champion
        EquipmentItem("Rocket Spear", "Royal Champion", true, 27, "Extended range and high burst damage snipping key defenses from safety."),
        EquipmentItem("Electro Boots", "Royal Champion", true, 27, "Passive chain lightning shocks surrounding enemy defenses."),
        EquipmentItem("Haste Vial", "Royal Champion", false, 18, "Extreme attack speed and movement boost.")
    )

    // Calculates Ores needed for equipment
    fun calculateOreRequirements(
        currentLevel: Int,
        targetLevel: Int,
        isEpic: Boolean
    ): OreCost {
        var shiny = 0
        var glowy = 0
        var starry = 0

        for (lvl in (currentLevel + 1)..targetLevel) {
            if (isEpic) {
                shiny += when {
                    lvl <= 9 -> 400
                    lvl <= 18 -> 800
                    else -> 1400
                }
                glowy += when {
                    lvl % 3 == 0 -> 100
                    else -> 40
                }
                starry += when {
                    lvl % 3 == 0 && lvl >= 9 -> 10
                    else -> 0
                }
            } else {
                shiny += when {
                    lvl <= 9 -> 300
                    else -> 600
                }
                glowy += when {
                    lvl % 3 == 0 -> 60
                    else -> 20
                }
            }
        }

        return OreCost(shiny, glowy, starry)
    }

    data class OreCost(val shinyOre: Int, val glowyOre: Int, val starryOre: Int)

    // Player Offense Completion Assessment
    data class OffenseProgress(
        val overallPercent: Int,
        val heroesPercent: Int,
        val troopsPercent: Int,
        val spellsPercent: Int,
        val equipmentPercent: Int,
        val heroesRemainingLevels: Int,
        val troopsRemainingLevels: Int,
        val spellsRemainingLevels: Int,
        val statusMessage: String
    )

    fun evaluatePlayerOffense(player: PlayerDetail): OffenseProgress {
        // Heroes
        var heroCurrent = 0
        var heroMax = 0
        player.heroes.filter { it.village == "home" }.forEach {
            heroCurrent += it.level
            heroMax += it.maxLevel
        }
        val heroPct = if (heroMax > 0) (heroCurrent.toFloat() / heroMax * 100).toInt() else 100
        val heroRemain = (heroMax - heroCurrent).coerceAtLeast(0)

        // Troops
        var troopCurrent = 0
        var troopMax = 0
        player.troops.filter { it.village == "home" }.forEach {
            troopCurrent += it.level
            troopMax += it.maxLevel
        }
        val troopPct = if (troopMax > 0) (troopCurrent.toFloat() / troopMax * 100).toInt() else 100
        val troopRemain = (troopMax - troopCurrent).coerceAtLeast(0)

        // Spells
        var spellCurrent = 0
        var spellMax = 0
        player.spells.filter { it.village == "home" }.forEach {
            spellCurrent += it.level
            spellMax += it.maxLevel
        }
        val spellPct = if (spellMax > 0) (spellCurrent.toFloat() / spellMax * 100).toInt() else 100
        val spellRemain = (spellMax - spellCurrent).coerceAtLeast(0)

        // Equipment
        var equipCurrent = 0
        var equipMax = 0
        player.heroEquipment.forEach {
            equipCurrent += it.level
            equipMax += it.maxLevel
        }
        val equipPct = if (equipMax > 0) (equipCurrent.toFloat() / equipMax * 100).toInt() else 100

        val totalCurrent = heroCurrent + troopCurrent + spellCurrent + equipCurrent
        val totalMax = (heroMax + troopMax + spellMax + equipMax).coerceAtLeast(1)
        val overall = (totalCurrent.toFloat() / totalMax * 100).toInt().coerceIn(0, 100)

        val status = when {
            overall >= 95 -> "Village Fully Maxed / War Ready"
            overall >= 80 -> "High Town Hall Maturity (Late Stage)"
            overall >= 60 -> "Mid-Tier Town Hall Progression"
            overall >= 40 -> "Developing Town Hall (Focus Offense)"
            else -> "Fresh Town Hall / Strategic Rush"
        }

        return OffenseProgress(
            overallPercent = overall,
            heroesPercent = heroPct,
            troopsPercent = troopPct,
            spellsPercent = spellPct,
            equipmentPercent = equipPct,
            heroesRemainingLevels = heroRemain,
            troopsRemainingLevels = troopRemain,
            spellsRemainingLevels = spellRemain,
            statusMessage = status
        )
    }

    data class HeroMetaBuild(
        val heroName: String,
        val buildName: String,
        val primaryEquipment: String,
        val secondaryEquipment: String,
        val tier: String,
        val playstyle: String,
        val keyPowerSpike: String
    )

    val META_BUILDS: List<HeroMetaBuild> = listOf(
        HeroMetaBuild(
            heroName = "Barbarian King",
            buildName = "Colossal Smash",
            primaryEquipment = "Giant Gauntlet",
            secondaryEquipment = "Rage Vial",
            tier = "S-Tier (Meta #1)",
            playstyle = "Massive area-of-effect destruction + huge self-heal and enraged speed inside enemy cores.",
            keyPowerSpike = "Giant Gauntlet Level 15 (unravels base cores)"
        ),
        HeroMetaBuild(
            heroName = "Barbarian King",
            buildName = "Compartment Shatterer",
            primaryEquipment = "Spiky Ball",
            secondaryEquipment = "Giant Gauntlet",
            tier = "S-Tier (Pro War)",
            playstyle = "Snipes up to 9 key defenses from distance while staying invulnerable in core.",
            keyPowerSpike = "Spiky Ball Level 18 (9 ricochets)"
        ),
        HeroMetaBuild(
            heroName = "Archer Queen",
            buildName = "Queen Charge Immortal",
            primaryEquipment = "Invisibility Vial",
            secondaryEquipment = "Healer Puppet",
            tier = "S-Tier (Standard)",
            playstyle = "Spawns private high-level healers while invisible, recovering full health without housing space.",
            keyPowerSpike = "Healer Puppet Level 15 (spawns 3 max healers)"
        ),
        HeroMetaBuild(
            heroName = "Archer Queen",
            buildName = "Defensive Sniper",
            primaryEquipment = "Frozen Arrow",
            secondaryEquipment = "Invisibility Vial",
            tier = "S-Tier (Hitrate)",
            playstyle = "Slows down opposing Town Halls, Monoliths, and Single Infernos by up to 75%.",
            keyPowerSpike = "Frozen Arrow Level 18 (75% slow duration)"
        ),
        HeroMetaBuild(
            heroName = "Grand Warden",
            buildName = "Ground & Air Protection",
            primaryEquipment = "Eternal Tome",
            secondaryEquipment = "Healing Tome",
            tier = "S-Tier (Universal)",
            playstyle = "Total invulnerability during Town Hall bomb explosion followed by sustained massive aura heal.",
            keyPowerSpike = "Eternal Tome Level 15 (8.5s immunity) + Healing Tome Level 15"
        ),
        HeroMetaBuild(
            heroName = "Grand Warden",
            buildName = "Nuclear Fireball Walk",
            primaryEquipment = "Fireball",
            secondaryEquipment = "Rage Gem",
            tier = "S-Tier (Triple Specialist)",
            playstyle = "Wipes out entire defense clusters (Eagle, Scattershots, CC) in a single devastating blast.",
            keyPowerSpike = "Fireball Level 24 (4,500+ direct damage)"
        ),
        HeroMetaBuild(
            heroName = "Royal Champion",
            buildName = "Blitzkrieg Sniper",
            primaryEquipment = "Rocket Spear",
            secondaryEquipment = "Haste Vial",
            tier = "S-Tier (2026 Meta)",
            playstyle = "Snipes core defenses from 11 tiles away before activating supercharged attack speed.",
            keyPowerSpike = "Rocket Spear Level 18 (8 long-range shots)"
        ),
        HeroMetaBuild(
            heroName = "Royal Champion",
            buildName = "Chain Shock Tank",
            primaryEquipment = "Electro Boots",
            secondaryEquipment = "Haste Vial",
            tier = "A-Tier (Clean Up)",
            playstyle = "Continuous chain lightning shocks point defenses as she glides through compartments.",
            keyPowerSpike = "Electro Boots Level 18 (4-target chain)"
        )
    )
}
