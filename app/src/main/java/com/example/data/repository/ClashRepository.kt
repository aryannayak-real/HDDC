package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entity.ClanSnapshotEntity
import com.example.data.local.entity.PersonalNoteEntity
import com.example.data.local.entity.PlayerSnapshotEntity
import com.example.data.local.entity.SettingsEntity
import com.example.data.local.entity.TrackedClanEntity
import com.example.data.local.entity.TrackedPlayerEntity
import com.example.data.local.entity.WarAssignmentEntity
import com.example.data.model.ClanDetail
import com.example.data.model.CurrentWar
import com.example.data.model.CwlGroup
import com.example.data.model.PlayerDetail
import com.example.data.model.WarLogEntry
import com.example.data.model.CapitalRaidSeason
import com.example.data.remote.ClashApiClient
import com.example.data.security.SecurityManager
import com.example.util.TagValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import org.json.JSONObject

class ClashRepository(
    private val database: AppDatabase,
    private val apiClient: ClashApiClient = ClashApiClient()
) {
    val settingsFlow: Flow<SettingsEntity?> = database.settingsDao().getSettingsFlow()
    val trackedPlayersFlow: Flow<List<TrackedPlayerEntity>> = database.trackedPlayerDao().getAllTrackedPlayers()
    val trackedClansFlow: Flow<List<TrackedClanEntity>> = database.trackedClanDao().getAllTrackedClans()
    val recentSnapshotsFlow: Flow<List<PlayerSnapshotEntity>> = database.snapshotDao().getRecentPlayerSnapshots()

    suspend fun getSettings(): SettingsEntity {
        return database.settingsDao().getSettings() ?: SettingsEntity()
    }

    suspend fun saveSettings(
        apiKeyPlain: String,
        proxyHost: String,
        proxyPort: Int,
        proxyUsername: String,
        proxyPasswordPlain: String,
        useProxy: Boolean
    ) {
        val current = getSettings()
        val encryptedKey = if (apiKeyPlain.isNotBlank()) {
            SecurityManager.encrypt(apiKeyPlain)
        } else {
            current.apiKeyEncrypted
        }
        val encryptedProxyPass = if (proxyPasswordPlain.isNotBlank()) {
            SecurityManager.encrypt(proxyPasswordPlain)
        } else {
            current.proxyPasswordEncrypted
        }

        val updated = current.copy(
            apiKeyEncrypted = encryptedKey,
            proxyHost = proxyHost.trim(),
            proxyPort = proxyPort,
            proxyUsername = proxyUsername.trim(),
            proxyPasswordEncrypted = encryptedProxyPass,
            useProxy = useProxy
        )
        database.settingsDao().saveSettings(updated)
    }

    suspend fun testConnection(): ClashApiClient.ConnectionTestResult {
        val settings = getSettings()
        val apiKey = SecurityManager.decrypt(settings.apiKeyEncrypted)
        val proxyPass = SecurityManager.decrypt(settings.proxyPasswordEncrypted)

        val result = apiClient.testConnection(
            apiKey = apiKey,
            proxyHost = settings.proxyHost,
            proxyPort = settings.proxyPort,
            proxyUser = settings.proxyUsername,
            proxyPass = proxyPass,
            useProxy = settings.useProxy
        )

        val updated = settings.copy(
            lastTestedAt = System.currentTimeMillis(),
            lastTestStatus = if (result.success) "SUCCESS" else "FAILED",
            lastTestMessage = "${result.message} - ${result.errorDetails ?: ""}"
        )
        database.settingsDao().saveSettings(updated)
        return result
    }

    suspend fun searchPlayer(rawTag: String): Result<PlayerDetail> {
        val tag = TagValidator.normalize(rawTag)
        val settings = getSettings()
        val apiKey = SecurityManager.decrypt(settings.apiKeyEncrypted)
        val proxyPass = SecurityManager.decrypt(settings.proxyPasswordEncrypted)

        return apiClient.getPlayer(
            tag = tag,
            apiKey = apiKey,
            proxyHost = settings.proxyHost,
            proxyPort = settings.proxyPort,
            proxyUser = settings.proxyUsername,
            proxyPass = proxyPass,
            useProxy = settings.useProxy
        ).map { it.first }
    }

    suspend fun trackPlayer(player: PlayerDetail, rawJson: String = "") {
        val entity = TrackedPlayerEntity(
            tag = player.tag,
            name = player.name,
            townHallLevel = player.townHallLevel,
            expLevel = player.expLevel,
            trophies = player.trophies,
            bestTrophies = player.bestTrophies,
            warStars = player.warStars,
            attackWins = player.attackWins,
            defenseWins = player.defenseWins,
            clanTag = player.clan?.tag,
            clanName = player.clan?.name,
            clanBadgeUrl = player.clan?.badgeUrls?.medium ?: player.clan?.badgeUrls?.small,
            donations = player.donations,
            donationsReceived = player.donationsReceived,
            lastUpdated = System.currentTimeMillis(),
            rawJson = rawJson
        )
        database.trackedPlayerDao().insertOrUpdate(entity)

        // Record a snapshot for historical analytics
        val snapshot = PlayerSnapshotEntity(
            playerTag = player.tag,
            timestamp = System.currentTimeMillis(),
            trophies = player.trophies,
            donations = player.donations,
            donationsReceived = player.donationsReceived,
            attackWins = player.attackWins,
            warStars = player.warStars
        )
        database.snapshotDao().insertPlayerSnapshot(snapshot)
    }

    suspend fun untrackPlayer(tag: String) {
        database.trackedPlayerDao().deleteByTag(tag)
    }

    suspend fun refreshPlayer(tag: String): Result<PlayerDetail> {
        val settings = getSettings()
        val apiKey = SecurityManager.decrypt(settings.apiKeyEncrypted)
        val proxyPass = SecurityManager.decrypt(settings.proxyPasswordEncrypted)

        val result = apiClient.getPlayer(
            tag = tag,
            apiKey = apiKey,
            proxyHost = settings.proxyHost,
            proxyPort = settings.proxyPort,
            proxyUser = settings.proxyUsername,
            proxyPass = proxyPass,
            useProxy = settings.useProxy
        )

        return result.map { (player, rawJson) ->
            trackPlayer(player, rawJson)
            player
        }
    }

    fun getPlayerSnapshots(playerTag: String): Flow<List<PlayerSnapshotEntity>> {
        return database.snapshotDao().getPlayerSnapshots(playerTag)
    }

    suspend fun searchClan(rawTag: String): Result<ClanDetail> {
        val tag = TagValidator.normalize(rawTag)
        val settings = getSettings()
        val apiKey = SecurityManager.decrypt(settings.apiKeyEncrypted)
        val proxyPass = SecurityManager.decrypt(settings.proxyPasswordEncrypted)

        return apiClient.getClan(
            tag = tag,
            apiKey = apiKey,
            proxyHost = settings.proxyHost,
            proxyPort = settings.proxyPort,
            proxyUser = settings.proxyUsername,
            proxyPass = proxyPass,
            useProxy = settings.useProxy
        ).map { it.first }
    }

    suspend fun trackClan(clan: ClanDetail, rawJson: String = "") {
        val entity = TrackedClanEntity(
            tag = clan.tag,
            name = clan.name,
            clanLevel = clan.clanLevel,
            membersCount = clan.members,
            clanPoints = clan.clanPoints,
            clanCapitalPoints = clan.clanCapitalPoints,
            capitalHallLevel = clan.capitalHallLevel,
            badgeUrl = clan.badgeUrls?.medium ?: clan.badgeUrls?.small,
            warWins = clan.warWins,
            warWinStreak = clan.warWinStreak,
            warFrequency = clan.warFrequency,
            lastUpdated = System.currentTimeMillis(),
            rawJson = rawJson
        )
        database.trackedClanDao().insertOrUpdate(entity)

        val snapshot = ClanSnapshotEntity(
            clanTag = clan.tag,
            timestamp = System.currentTimeMillis(),
            clanPoints = clan.clanPoints,
            membersCount = clan.members,
            warWins = clan.warWins,
            warWinStreak = clan.warWinStreak
        )
        database.snapshotDao().insertClanSnapshot(snapshot)
    }

    suspend fun untrackClan(tag: String) {
        database.trackedClanDao().deleteByTag(tag)
    }

    suspend fun refreshClan(tag: String): Result<ClanDetail> {
        val settings = getSettings()
        val apiKey = SecurityManager.decrypt(settings.apiKeyEncrypted)
        val proxyPass = SecurityManager.decrypt(settings.proxyPasswordEncrypted)

        val result = apiClient.getClan(
            tag = tag,
            apiKey = apiKey,
            proxyHost = settings.proxyHost,
            proxyPort = settings.proxyPort,
            proxyUser = settings.proxyUsername,
            proxyPass = proxyPass,
            useProxy = settings.useProxy
        )

        return result.map { (clan, rawJson) ->
            trackClan(clan, rawJson)
            clan
        }
    }

    fun getClanSnapshots(clanTag: String): Flow<List<ClanSnapshotEntity>> {
        return database.snapshotDao().getClanSnapshots(clanTag)
    }

    suspend fun fetchCurrentWar(clanTag: String): Result<CurrentWar> {
        val settings = getSettings()
        val apiKey = SecurityManager.decrypt(settings.apiKeyEncrypted)
        val proxyPass = SecurityManager.decrypt(settings.proxyPasswordEncrypted)

        return apiClient.getCurrentWar(
            clanTag = TagValidator.normalize(clanTag),
            apiKey = apiKey,
            proxyHost = settings.proxyHost,
            proxyPort = settings.proxyPort,
            proxyUser = settings.proxyUsername,
            proxyPass = proxyPass,
            useProxy = settings.useProxy
        )
    }

    suspend fun fetchCwlGroup(clanTag: String): Result<CwlGroup> {
        val settings = getSettings()
        val apiKey = SecurityManager.decrypt(settings.apiKeyEncrypted)
        val proxyPass = SecurityManager.decrypt(settings.proxyPasswordEncrypted)

        return apiClient.getCwlGroup(
            clanTag = TagValidator.normalize(clanTag),
            apiKey = apiKey,
            proxyHost = settings.proxyHost,
            proxyPort = settings.proxyPort,
            proxyUser = settings.proxyUsername,
            proxyPass = proxyPass,
            useProxy = settings.useProxy
        )
    }

    suspend fun fetchClanWarLog(clanTag: String): Result<List<WarLogEntry>> {
        val settings = getSettings()
        val apiKey = SecurityManager.decrypt(settings.apiKeyEncrypted)
        val proxyPass = SecurityManager.decrypt(settings.proxyPasswordEncrypted)

        return apiClient.getClanWarLog(
            clanTag = TagValidator.normalize(clanTag),
            apiKey = apiKey,
            proxyHost = settings.proxyHost,
            proxyPort = settings.proxyPort,
            proxyUser = settings.proxyUsername,
            proxyPass = proxyPass,
            useProxy = settings.useProxy
        )
    }

    suspend fun fetchCapitalRaidSeasons(clanTag: String): Result<List<CapitalRaidSeason>> {
        val settings = getSettings()
        val apiKey = SecurityManager.decrypt(settings.apiKeyEncrypted)
        val proxyPass = SecurityManager.decrypt(settings.proxyPasswordEncrypted)

        return apiClient.getCapitalRaidSeasons(
            clanTag = TagValidator.normalize(clanTag),
            apiKey = apiKey,
            proxyHost = settings.proxyHost,
            proxyPort = settings.proxyPort,
            proxyUser = settings.proxyUsername,
            proxyPass = proxyPass,
            useProxy = settings.useProxy
        )
    }

    // War Attack Planner
    fun getWarAssignments(clanTag: String): Flow<List<WarAssignmentEntity>> {
        return database.warAssignmentDao().getAssignmentsForClan(clanTag)
    }

    suspend fun saveWarAssignment(assignment: WarAssignmentEntity) {
        database.warAssignmentDao().insertAssignment(assignment)
    }

    suspend fun deleteWarAssignment(id: Long) {
        database.warAssignmentDao().deleteAssignmentById(id)
    }

    // Notes
    fun getNotes(type: String, tag: String): Flow<List<PersonalNoteEntity>> {
        return database.personalNoteDao().getNotes(type, tag)
    }

    suspend fun saveNote(note: PersonalNoteEntity) {
        database.personalNoteDao().insertNote(note)
    }

    suspend fun deleteNote(id: Long) {
        database.personalNoteDao().deleteNoteById(id)
    }

    // Cached Detail Loaders
    suspend fun getCachedPlayer(tag: String): PlayerDetail? {
        val entity = database.trackedPlayerDao().getPlayerByTag(tag) ?: return null
        return try {
            if (entity.rawJson.isNotBlank()) {
                apiClient.parsePlayer(JSONObject(entity.rawJson))
            } else {
                PlayerDetail(
                    tag = entity.tag,
                    name = entity.name,
                    townHallLevel = entity.townHallLevel,
                    expLevel = entity.expLevel,
                    trophies = entity.trophies,
                    bestTrophies = entity.bestTrophies,
                    warStars = entity.warStars,
                    attackWins = entity.attackWins,
                    defenseWins = entity.defenseWins,
                    donations = entity.donations,
                    donationsReceived = entity.donationsReceived
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getCachedClan(tag: String): ClanDetail? {
        val entity = database.trackedClanDao().getClanByTag(tag) ?: return null
        return try {
            if (entity.rawJson.isNotBlank()) {
                apiClient.parseClan(JSONObject(entity.rawJson))
            } else {
                ClanDetail(
                    tag = entity.tag,
                    name = entity.name,
                    clanLevel = entity.clanLevel,
                    members = entity.membersCount,
                    clanPoints = entity.clanPoints,
                    clanCapitalPoints = entity.clanCapitalPoints,
                    capitalHallLevel = entity.capitalHallLevel,
                    warWins = entity.warWins,
                    warWinStreak = entity.warWinStreak,
                    warFrequency = entity.warFrequency
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    // Active Builder & Lab Scheduling
    val activeBuildersFlow: Flow<List<com.example.data.local.entity.ActiveBuilderEntity>> =
        database.activeBuilderDao().getAllBuilders()

    suspend fun saveActiveBuilder(builder: com.example.data.local.entity.ActiveBuilderEntity) {
        database.activeBuilderDao().insertOrUpdate(builder)
    }

    suspend fun initDefaultBuilders() {
        val defaultList = listOf(
            com.example.data.local.entity.ActiveBuilderEntity(1, "Builder 1", "Idle", 0, 0.0, 0L, false),
            com.example.data.local.entity.ActiveBuilderEntity(2, "Builder 2", "Idle", 0, 0.0, 0L, false),
            com.example.data.local.entity.ActiveBuilderEntity(3, "Builder 3", "Idle", 0, 0.0, 0L, false),
            com.example.data.local.entity.ActiveBuilderEntity(4, "Builder 4", "Idle", 0, 0.0, 0L, false),
            com.example.data.local.entity.ActiveBuilderEntity(5, "Builder 5", "Idle", 0, 0.0, 0L, false),
            com.example.data.local.entity.ActiveBuilderEntity(6, "B.O.B / 6th Builder", "Idle", 0, 0.0, 0L, false),
            com.example.data.local.entity.ActiveBuilderEntity(7, "Laboratory", "Idle", 0, 0.0, 0L, false)
        )
        database.activeBuilderDao().insertAll(defaultList)
    }
}
