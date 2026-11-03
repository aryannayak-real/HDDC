package com.example.data.model

data class BadgeUrls(
    val small: String? = null,
    val medium: String? = null,
    val large: String? = null
)

data class IconUrls(
    val small: String? = null,
    val tiny: String? = null,
    val medium: String? = null
)

data class League(
    val id: Int? = null,
    val name: String? = null,
    val iconUrls: IconUrls? = null
)

data class PlayerClan(
    val tag: String,
    val name: String,
    val clanLevel: Int? = null,
    val badgeUrls: BadgeUrls? = null
)

data class UnitItem(
    val name: String,
    val level: Int,
    val maxLevel: Int,
    val village: String = "home"
)

data class Achievement(
    val name: String,
    val stars: Int = 0,
    val value: Long = 0L,
    val target: Long = 0L,
    val info: String = "",
    val completionInfo: String? = null
)

data class PlayerDetail(
    val tag: String,
    val name: String,
    val townHallLevel: Int = 1,
    val townHallWeaponLevel: Int = 0,
    val expLevel: Int = 1,
    val trophies: Int = 0,
    val bestTrophies: Int = 0,
    val warStars: Int = 0,
    val attackWins: Int = 0,
    val defenseWins: Int = 0,
    val builderHallLevel: Int? = null,
    val builderBaseTrophies: Int = 0,
    val bestBuilderBaseTrophies: Int = 0,
    val donations: Int = 0,
    val donationsReceived: Int = 0,
    val role: String? = null,
    val warPreference: String? = null,
    val clan: PlayerClan? = null,
    val league: League? = null,
    val heroes: List<UnitItem> = emptyList(),
    val heroEquipment: List<UnitItem> = emptyList(),
    val troops: List<UnitItem> = emptyList(),
    val spells: List<UnitItem> = emptyList(),
    val achievements: List<Achievement> = emptyList()
)

data class ClanMember(
    val tag: String,
    val name: String,
    val role: String = "member",
    val expLevel: Int = 1,
    val league: League? = null,
    val trophies: Int = 0,
    val builderBaseTrophies: Int = 0,
    val clanRank: Int = 0,
    val previousClanRank: Int = 0,
    val donations: Int = 0,
    val donationsReceived: Int = 0,
    val townHallLevel: Int = 0
)

data class ClanDetail(
    val tag: String,
    val name: String,
    val type: String = "open",
    val description: String = "",
    val location: String? = null,
    val badgeUrls: BadgeUrls? = null,
    val clanLevel: Int = 1,
    val clanPoints: Int = 0,
    val clanBuilderBasePoints: Int = 0,
    val clanCapitalPoints: Int = 0,
    val capitalHallLevel: Int = 0,
    val requiredTrophies: Int = 0,
    val warFrequency: String? = null,
    val warWinStreak: Int = 0,
    val warWins: Int = 0,
    val warTies: Int = 0,
    val warLosses: Int = 0,
    val isWarLogPublic: Boolean = false,
    val warLeague: String? = null,
    val members: Int = 0,
    val memberList: List<ClanMember> = emptyList()
)

data class WarAttack(
    val attackerTag: String,
    val defenderTag: String,
    val stars: Int,
    val destructionPercentage: Double,
    val order: Int = 0,
    val duration: Int = 0
)

data class WarMember(
    val tag: String,
    val name: String,
    val mapPosition: Int = 1,
    val townhallLevel: Int = 1,
    val attacks: List<WarAttack> = emptyList(),
    val opponentAttacks: Int = 0,
    val bestOpponentAttack: WarAttack? = null
)

data class WarClan(
    val tag: String,
    val name: String,
    val badgeUrls: BadgeUrls? = null,
    val clanLevel: Int = 1,
    val attacks: Int = 0,
    val stars: Int = 0,
    val destructionPercentage: Double = 0.0,
    val members: List<WarMember> = emptyList()
)

data class CurrentWar(
    val state: String = "notInWar", // notInWar, preparation, inWar, warEnded
    val teamSize: Int = 0,
    val attacksPerMember: Int = 1,
    val battleModifier: String? = null,
    val preparationStartTime: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val clan: WarClan? = null,
    val opponent: WarClan? = null
)

data class CwlClan(
    val tag: String,
    val name: String,
    val clanLevel: Int = 1,
    val badgeUrls: BadgeUrls? = null
)

data class CwlRound(
    val warTags: List<String> = emptyList()
)

data class CwlGroup(
    val state: String = "notInWar",
    val season: String = "",
    val clans: List<CwlClan> = emptyList(),
    val rounds: List<CwlRound> = emptyList()
)

data class WarLogClan(
    val tag: String? = null,
    val name: String? = null,
    val badgeUrls: BadgeUrls? = null,
    val clanLevel: Int = 1,
    val attacks: Int = 0,
    val stars: Int = 0,
    val destructionPercentage: Double = 0.0,
    val expEarned: Int = 0
)

data class WarLogEntry(
    val result: String? = null, // "win", "lose", "tie"
    val endTime: String? = null,
    val teamSize: Int = 0,
    val attacksPerMember: Int = 1,
    val battleModifier: String? = null,
    val clan: WarLogClan,
    val opponent: WarLogClan
)

data class CapitalRaidMember(
    val tag: String,
    val name: String,
    val attacks: Int = 0,
    val attackLimit: Int = 5,
    val bonusAttackLimit: Int = 1,
    val capitalResourcesLooted: Int = 0
)

data class CapitalRaidSeason(
    val state: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val capitalTotalLoot: Int = 0,
    val raidsCompleted: Int = 0,
    val totalAttacks: Int = 0,
    val enemyDistrictsDestroyed: Int = 0,
    val offensiveReward: Int = 0,
    val defensiveReward: Int = 0,
    val members: List<CapitalRaidMember> = emptyList()
)

