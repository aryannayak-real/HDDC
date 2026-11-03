package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 1,
    val apiKeyEncrypted: String = "",
    val proxyHost: String = "46.173.78.114",
    val proxyPort: Int = 4443,
    val proxyUsername: String = "",
    val proxyPasswordEncrypted: String = "",
    val useProxy: Boolean = true,
    val lastTestedAt: Long = 0L,
    val lastTestStatus: String = "NOT_TESTED",
    val lastTestMessage: String = "Connection has not been tested yet."
)

@Entity(tableName = "tracked_players")
data class TrackedPlayerEntity(
    @PrimaryKey val tag: String,
    val name: String,
    val townHallLevel: Int,
    val expLevel: Int,
    val trophies: Int,
    val bestTrophies: Int,
    val warStars: Int,
    val attackWins: Int,
    val defenseWins: Int,
    val clanTag: String? = null,
    val clanName: String? = null,
    val clanBadgeUrl: String? = null,
    val donations: Int = 0,
    val donationsReceived: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis(),
    val rawJson: String = ""
)

@Entity(tableName = "tracked_clans")
data class TrackedClanEntity(
    @PrimaryKey val tag: String,
    val name: String,
    val clanLevel: Int,
    val membersCount: Int,
    val clanPoints: Int,
    val clanCapitalPoints: Int = 0,
    val capitalHallLevel: Int = 0,
    val badgeUrl: String? = null,
    val warWins: Int = 0,
    val warWinStreak: Int = 0,
    val warFrequency: String? = null,
    val lastUpdated: Long = System.currentTimeMillis(),
    val rawJson: String = ""
)

@Entity(tableName = "player_snapshots")
data class PlayerSnapshotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val playerTag: String,
    val timestamp: Long = System.currentTimeMillis(),
    val trophies: Int,
    val donations: Int,
    val donationsReceived: Int,
    val attackWins: Int,
    val warStars: Int
)

@Entity(tableName = "clan_snapshots")
data class ClanSnapshotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clanTag: String,
    val timestamp: Long = System.currentTimeMillis(),
    val clanPoints: Int,
    val membersCount: Int,
    val warWins: Int,
    val warWinStreak: Int
)

@Entity(tableName = "war_assignments")
data class WarAssignmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clanTag: String,
    val opponentTag: String = "",
    val assignedMemberTag: String,
    val assignedMemberName: String,
    val targetBaseNumber: Int,
    val targetBaseTownHall: Int = 0,
    val notes: String = "",
    val status: String = "PLANNED", // PLANNED, 3_STAR, 2_STAR, 1_STAR, FAILED
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "personal_notes")
data class PersonalNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val targetType: String, // PLAYER, CLAN, WAR
    val targetTag: String,
    val title: String,
    val content: String,
    val updatedAt: Long = System.currentTimeMillis()
)
