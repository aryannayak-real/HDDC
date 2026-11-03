package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
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
import com.example.data.repository.ClashRepository
import com.example.data.security.SecurityManager
import com.example.util.TagValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppNavTab(val title: String) {
    DASHBOARD("Dashboard"),
    PLAYERS("Players"),
    CLANS("Clans"),
    WAR("War & CWL"),
    ANALYTICS("Analytics"),
    CALCULATORS("Planner"),
    SETTINGS("Settings")
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val repository = ClashRepository(database)

    // Current Navigation Screen
    private val _currentTab = MutableStateFlow(AppNavTab.DASHBOARD)
    val currentTab: StateFlow<AppNavTab> = _currentTab.asStateFlow()

    // Settings
    val settingsState: StateFlow<SettingsEntity?> = repository.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // Tracked Lists
    val trackedPlayers: StateFlow<List<TrackedPlayerEntity>> = repository.trackedPlayersFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val trackedClans: StateFlow<List<TrackedClanEntity>> = repository.trackedClansFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val recentSnapshots: StateFlow<List<PlayerSnapshotEntity>> = repository.recentSnapshotsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Selected Player View
    private val _selectedPlayer = MutableStateFlow<PlayerDetail?>(null)
    val selectedPlayer: StateFlow<PlayerDetail?> = _selectedPlayer.asStateFlow()

    private val _playerSnapshots = MutableStateFlow<List<PlayerSnapshotEntity>>(emptyList())
    val playerSnapshots: StateFlow<List<PlayerSnapshotEntity>> = _playerSnapshots.asStateFlow()

    // Selected Clan View
    private val _selectedClan = MutableStateFlow<ClanDetail?>(null)
    val selectedClan: StateFlow<ClanDetail?> = _selectedClan.asStateFlow()

    // War & CWL View
    private val _currentWar = MutableStateFlow<CurrentWar?>(null)
    val currentWar: StateFlow<CurrentWar?> = _currentWar.asStateFlow()

    private val _cwlGroup = MutableStateFlow<CwlGroup?>(null)
    val cwlGroup: StateFlow<CwlGroup?> = _cwlGroup.asStateFlow()

    private val _warAssignments = MutableStateFlow<List<WarAssignmentEntity>>(emptyList())
    val warAssignments: StateFlow<List<WarAssignmentEntity>> = _warAssignments.asStateFlow()

    private val _clanWarLog = MutableStateFlow<List<WarLogEntry>>(emptyList())
    val clanWarLog: StateFlow<List<WarLogEntry>> = _clanWarLog.asStateFlow()

    private val _capitalRaidSeasons = MutableStateFlow<List<CapitalRaidSeason>>(emptyList())
    val capitalRaidSeasons: StateFlow<List<CapitalRaidSeason>> = _capitalRaidSeasons.asStateFlow()

    val activeBuilders: StateFlow<List<com.example.data.local.entity.ActiveBuilderEntity>> =
        repository.activeBuildersFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            repository.activeBuildersFlow.collect { list ->
                if (list.isEmpty()) {
                    repository.initDefaultBuilders()
                }
            }
        }
    }

    // Loading & Error States
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    // Test Connection State
    private val _connectionTestResult = MutableStateFlow<ClashApiClient.ConnectionTestResult?>(null)
    val connectionTestResult: StateFlow<ClashApiClient.ConnectionTestResult?> = _connectionTestResult.asStateFlow()

    fun switchTab(tab: AppNavTab) {
        _currentTab.value = tab
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }

    // SEARCH & SELECTION
    fun searchPlayer(tag: String) {
        if (!TagValidator.isValid(tag)) {
            _errorMessage.value = "Invalid tag format. Supercell tags contain 0, 2, 8, 9, P, Y, L, Q, G, R, J, C, U, V."
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            repository.searchPlayer(tag).fold(
                onSuccess = { player ->
                    _selectedPlayer.value = player
                    loadPlayerSnapshots(player.tag)
                    _currentTab.value = AppNavTab.PLAYERS
                },
                onFailure = { error ->
                    _errorMessage.value = error.localizedMessage ?: "Failed to find player."
                }
            )
            _isLoading.value = false
        }
    }

    fun selectTrackedPlayer(tag: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val cached = repository.getCachedPlayer(tag)
            if (cached != null) {
                _selectedPlayer.value = cached
                loadPlayerSnapshots(tag)
            }
            // Also refresh in background
            repository.refreshPlayer(tag).fold(
                onSuccess = { refreshed ->
                    _selectedPlayer.value = refreshed
                    loadPlayerSnapshots(tag)
                },
                onFailure = { }
            )
            _isLoading.value = false
            _currentTab.value = AppNavTab.PLAYERS
        }
    }

    private fun loadPlayerSnapshots(tag: String) {
        viewModelScope.launch {
            repository.getPlayerSnapshots(tag).collect { list ->
                _playerSnapshots.value = list
            }
        }
    }

    fun toggleTrackPlayer(player: PlayerDetail) {
        viewModelScope.launch {
            val isTracked = trackedPlayers.value.any { it.tag == player.tag }
            if (isTracked) {
                repository.untrackPlayer(player.tag)
                _successMessage.value = "Removed ${player.name} (${player.tag}) from tracking."
            } else {
                repository.trackPlayer(player)
                _successMessage.value = "Now tracking ${player.name} (${player.tag})!"
            }
        }
    }

    fun refreshCurrentPlayer() {
        val current = _selectedPlayer.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            repository.refreshPlayer(current.tag).fold(
                onSuccess = { updated ->
                    _selectedPlayer.value = updated
                    loadPlayerSnapshots(updated.tag)
                    _successMessage.value = "Updated ${updated.name}'s stats."
                },
                onFailure = { err ->
                    _errorMessage.value = err.localizedMessage ?: "Failed to refresh player stats."
                }
            )
            _isLoading.value = false
        }
    }

    // CLAN INTELLIGENCE
    fun searchClan(tag: String) {
        if (!TagValidator.isValid(tag)) {
            _errorMessage.value = "Invalid clan tag format. Please check the tag."
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            repository.searchClan(tag).fold(
                onSuccess = { clan ->
                    _selectedClan.value = clan
                    loadClanWar(clan.tag)
                    _currentTab.value = AppNavTab.CLANS
                },
                onFailure = { error ->
                    _errorMessage.value = error.localizedMessage ?: "Failed to find clan."
                }
            )
            _isLoading.value = false
        }
    }

    fun selectTrackedClan(tag: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val cached = repository.getCachedClan(tag)
            if (cached != null) {
                _selectedClan.value = cached
                loadClanWar(tag)
            }
            repository.refreshClan(tag).fold(
                onSuccess = { refreshed ->
                    _selectedClan.value = refreshed
                    loadClanWar(tag)
                },
                onFailure = { }
            )
            _isLoading.value = false
            _currentTab.value = AppNavTab.CLANS
        }
    }

    fun toggleTrackClan(clan: ClanDetail) {
        viewModelScope.launch {
            val isTracked = trackedClans.value.any { it.tag == clan.tag }
            if (isTracked) {
                repository.untrackClan(clan.tag)
                _successMessage.value = "Removed ${clan.name} from tracked clans."
            } else {
                repository.trackClan(clan)
                _successMessage.value = "Now tracking clan ${clan.name}!"
            }
        }
    }

    fun refreshCurrentClan() {
        val current = _selectedClan.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            repository.refreshClan(current.tag).fold(
                onSuccess = { updated ->
                    _selectedClan.value = updated
                    loadClanWar(updated.tag)
                    _successMessage.value = "Updated clan ${updated.name}."
                },
                onFailure = { err ->
                    _errorMessage.value = err.localizedMessage ?: "Failed to refresh clan."
                }
            )
            _isLoading.value = false
        }
    }

    // WAR & CWL
    fun loadClanWar(clanTag: String) {
        viewModelScope.launch {
            repository.fetchCurrentWar(clanTag).fold(
                onSuccess = { war ->
                    _currentWar.value = war
                },
                onFailure = {
                    // Current war might be private or not in war
                    _currentWar.value = CurrentWar(state = "notInWar")
                }
            )

            repository.fetchCwlGroup(clanTag).fold(
                onSuccess = { cwl ->
                    _cwlGroup.value = cwl
                },
                onFailure = {
                    _cwlGroup.value = null
                }
            )

            repository.fetchClanWarLog(clanTag).fold(
                onSuccess = { log ->
                    _clanWarLog.value = log
                },
                onFailure = {
                    _clanWarLog.value = emptyList()
                }
            )

            repository.fetchCapitalRaidSeasons(clanTag).fold(
                onSuccess = { seasons ->
                    _capitalRaidSeasons.value = seasons
                },
                onFailure = {
                    _capitalRaidSeasons.value = emptyList()
                }
            )

            repository.getWarAssignments(clanTag).collect { assignments ->
                _warAssignments.value = assignments
            }
        }
    }

    fun addWarAssignment(clanTag: String, memberTag: String, memberName: String, targetBase: Int, notes: String) {
        viewModelScope.launch {
            val assignment = WarAssignmentEntity(
                clanTag = clanTag,
                assignedMemberTag = memberTag,
                assignedMemberName = memberName,
                targetBaseNumber = targetBase,
                notes = notes,
                status = "PLANNED"
            )
            repository.saveWarAssignment(assignment)
        }
    }

    fun updateWarAssignmentStatus(assignment: WarAssignmentEntity, newStatus: String) {
        viewModelScope.launch {
            repository.saveWarAssignment(assignment.copy(status = newStatus))
        }
    }

    fun deleteWarAssignment(id: Long) {
        viewModelScope.launch {
            repository.deleteWarAssignment(id)
        }
    }

    // SETTINGS & CREDENTIALS
    fun saveCredentials(
        apiKey: String,
        proxyHost: String,
        proxyPort: Int,
        proxyUser: String,
        proxyPass: String,
        useProxy: Boolean
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.saveSettings(
                    apiKeyPlain = apiKey,
                    proxyHost = proxyHost,
                    proxyPort = proxyPort,
                    proxyUsername = proxyUser,
                    proxyPasswordPlain = proxyPass,
                    useProxy = useProxy
                )
                _successMessage.value = "Credentials encrypted and saved successfully."
            } catch (e: Exception) {
                _errorMessage.value = "Failed to save settings: ${e.localizedMessage}"
            }
            _isLoading.value = false
        }
    }

    fun testConnection() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _connectionTestResult.value = null
            val result = repository.testConnection()
            _connectionTestResult.value = result
            if (result.success) {
                _successMessage.value = "Supercell API Connection Verified! (${result.latencyMs}ms)"
            } else {
                _errorMessage.value = "${result.message}: ${result.errorDetails ?: ""}"
            }
            _isLoading.value = false
        }
    }

    // Builder & Lab Scheduler Actions
    fun setBuilderUpgrade(slotId: Int, slotName: String, target: String, level: Int, hours: Double) {
        viewModelScope.launch {
            val finishTime = if (hours > 0) System.currentTimeMillis() + (hours * 3600 * 1000).toLong() else 0L
            val builder = com.example.data.local.entity.ActiveBuilderEntity(
                slotId = slotId,
                slotName = slotName,
                upgradeTarget = target.ifBlank { "Upgrade" },
                targetLevel = level,
                durationHours = hours,
                finishTimeMillis = finishTime,
                isBusy = true
            )
            repository.saveActiveBuilder(builder)
        }
    }

    fun clearBuilder(slotId: Int, slotName: String) {
        viewModelScope.launch {
            val builder = com.example.data.local.entity.ActiveBuilderEntity(
                slotId = slotId,
                slotName = slotName,
                upgradeTarget = "Idle",
                targetLevel = 0,
                durationHours = 0.0,
                finishTimeMillis = 0L,
                isBusy = false
            )
            repository.saveActiveBuilder(builder)
        }
    }
}
