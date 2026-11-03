package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AppNavTab
import com.example.ui.MainViewModel
import com.example.ui.screens.analytics.AnalyticsScreen
import com.example.ui.screens.calculators.CalculatorsScreen
import com.example.ui.screens.clan.ClanScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.player.PlayerScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.war.WarScreen
import com.example.ui.theme.BackgroundNavy
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CyanHUD
import com.example.ui.theme.GemGreen
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.HDDCTheme
import com.example.ui.theme.LossRed
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            HDDCTheme {
                MainAppShell(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppShell(viewModel: MainViewModel) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val trackedPlayers by viewModel.trackedPlayers.collectAsStateWithLifecycle()
    val trackedClans by viewModel.trackedClans.collectAsStateWithLifecycle()
    val recentSnapshots by viewModel.recentSnapshots.collectAsStateWithLifecycle()
    val selectedPlayer by viewModel.selectedPlayer.collectAsStateWithLifecycle()
    val selectedClan by viewModel.selectedClan.collectAsStateWithLifecycle()
    val playerSnapshots by viewModel.playerSnapshots.collectAsStateWithLifecycle()
    val currentWar by viewModel.currentWar.collectAsStateWithLifecycle()
    val cwlGroup by viewModel.cwlGroup.collectAsStateWithLifecycle()
    val warAssignments by viewModel.warAssignments.collectAsStateWithLifecycle()
    val activeBuilders by viewModel.activeBuilders.collectAsStateWithLifecycle()
    val clanWarLog by viewModel.clanWarLog.collectAsStateWithLifecycle()
    val capitalRaidSeasons by viewModel.capitalRaidSeasons.collectAsStateWithLifecycle()
    val settings by viewModel.settingsState.collectAsStateWithLifecycle()
    val testResult by viewModel.connectionTestResult.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val successMessage by viewModel.successMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // Handle back button: return to dashboard if on another tab
    BackHandler(enabled = currentTab != AppNavTab.DASHBOARD) {
        viewModel.switchTab(AppNavTab.DASHBOARD)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BackgroundNavy,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBarHeader(
                currentTab = currentTab,
                isConfigured = !settings?.apiKeyEncrypted.isNullOrEmpty(),
                onSettingsClick = { viewModel.switchTab(AppNavTab.SETTINGS) }
            )
        },
        bottomBar = {
            HDDCBottomNavigationBar(
                currentTab = currentTab,
                onTabSelect = { viewModel.switchTab(it) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Floating Status / Error Alert Banner
            AnimatedVisibility(
                visible = errorMessage != null || successMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                val isErr = errorMessage != null
                val text = errorMessage ?: successMessage.orEmpty()
                val bannerColor = if (isErr) LossRed else GemGreen

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(bannerColor.copy(alpha = 0.2f))
                        .border(1.dp, bannerColor, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = text,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = bannerColor,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { viewModel.clearMessages() },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Dismiss", tint = bannerColor, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Screen Switcher
            when (currentTab) {
                AppNavTab.DASHBOARD -> DashboardScreen(
                    trackedPlayers = trackedPlayers,
                    trackedClans = trackedClans,
                    recentSnapshots = recentSnapshots,
                    onSearchPlayer = { viewModel.searchPlayer(it) },
                    onSearchClan = { viewModel.searchClan(it) },
                    onSelectPlayer = { viewModel.selectTrackedPlayer(it) },
                    onSelectClan = { viewModel.selectTrackedClan(it) },
                    onNavigateTab = { viewModel.switchTab(it) }
                )

                AppNavTab.PLAYERS -> PlayerScreen(
                    player = selectedPlayer,
                    trackedPlayers = trackedPlayers,
                    snapshots = playerSnapshots,
                    isLoading = isLoading,
                    onSearch = { viewModel.searchPlayer(it) },
                    onToggleTrack = { viewModel.toggleTrackPlayer(it) },
                    onRefresh = { viewModel.refreshCurrentPlayer() },
                    onInspectClan = { viewModel.searchClan(it) },
                    onNavigateToPlanner = { viewModel.switchTab(AppNavTab.CALCULATORS) }
                )

                AppNavTab.CLANS -> ClanScreen(
                    clan = selectedClan,
                    trackedClans = trackedClans,
                    isLoading = isLoading,
                    onSearch = { viewModel.searchClan(it) },
                    onToggleTrack = { viewModel.toggleTrackClan(it) },
                    onRefresh = { viewModel.refreshCurrentClan() },
                    onSelectMember = { viewModel.searchPlayer(it) },
                    onNavigateWar = { viewModel.switchTab(AppNavTab.WAR) },
                    warLog = clanWarLog,
                    capitalRaids = capitalRaidSeasons
                )

                AppNavTab.WAR -> WarScreen(
                    currentWar = currentWar,
                    cwlGroup = cwlGroup,
                    trackedClans = trackedClans,
                    warAssignments = warAssignments,
                    selectedClanTag = selectedClan?.tag ?: trackedClans.firstOrNull()?.tag,
                    onSelectClan = { viewModel.selectTrackedClan(it) },
                    onRefreshWar = { viewModel.loadClanWar(it) },
                    onAddAssignment = { clanTag, memberTag, memberName, targetBase, notes ->
                        viewModel.addWarAssignment(clanTag, memberTag, memberName, targetBase, notes)
                    },
                    onUpdateAssignmentStatus = { assignment, newStatus ->
                        viewModel.updateWarAssignmentStatus(assignment, newStatus)
                    },
                    onDeleteAssignment = { viewModel.deleteWarAssignment(it) }
                )

                AppNavTab.ANALYTICS -> AnalyticsScreen(
                    trackedPlayers = trackedPlayers,
                    trackedClans = trackedClans,
                    recentSnapshots = recentSnapshots
                )

                AppNavTab.CALCULATORS -> CalculatorsScreen(
                    selectedPlayer = selectedPlayer ?: trackedPlayers.firstOrNull()?.let { tp ->
                        com.example.data.model.PlayerDetail(
                            tag = tp.tag,
                            name = tp.name,
                            townHallLevel = tp.townHallLevel,
                            expLevel = tp.expLevel,
                            trophies = tp.trophies,
                            bestTrophies = tp.bestTrophies,
                            warStars = tp.warStars
                        )
                    },
                    activeBuilders = activeBuilders,
                    onSetBuilder = { slotId, slotName, target, level, hours ->
                        viewModel.setBuilderUpgrade(slotId, slotName, target, level, hours)
                    },
                    onClearBuilder = { slotId, slotName ->
                        viewModel.clearBuilder(slotId, slotName)
                    }
                )

                AppNavTab.SETTINGS -> SettingsScreen(
                    settings = settings,
                    testResult = testResult,
                    isLoading = isLoading,
                    onSave = { key, host, port, user, pass, useProxy ->
                        viewModel.saveCredentials(key, host, port, user, pass, useProxy)
                    },
                    onTestConnection = { viewModel.testConnection() }
                )
            }
        }
    }
}

@Composable
private fun TopAppBarHeader(
    currentTab: AppNavTab,
    isConfigured: Boolean,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .background(SurfaceDark)
            .border(1.dp, BorderSubtle)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceCard)
                    .border(1.dp, GoldPrimary, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "HDDC Logo",
                    tint = GoldBright,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "HDDC",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = GoldBright,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• ${currentTab.title.uppercase()}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                }
                Text(
                    text = "CLASH OF CLANS INTELLIGENCE",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 0.8.sp
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Status Chip
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isConfigured) GemGreen.copy(alpha = 0.2f) else LossRed.copy(alpha = 0.2f))
                    .border(1.dp, if (isConfigured) GemGreen else LossRed, CircleShape)
                    .clickable(onClick = onSettingsClick)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = if (isConfigured) "ONLINE" else "SETUP",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isConfigured) GemGreen else LossRed
                )
            }
        }
    }
}

@Composable
private fun HDDCBottomNavigationBar(
    currentTab: AppNavTab,
    onTabSelect: (AppNavTab) -> Unit
) {
    val navItems = listOf(
        NavItem(AppNavTab.DASHBOARD, Icons.Default.Dashboard, "Dash"),
        NavItem(AppNavTab.PLAYERS, Icons.Default.Person, "Players"),
        NavItem(AppNavTab.CLANS, Icons.Default.Groups, "Clans"),
        NavItem(AppNavTab.WAR, Icons.Default.MilitaryTech, "War"),
        NavItem(AppNavTab.ANALYTICS, Icons.Default.Insights, "Analytics"),
        NavItem(AppNavTab.CALCULATORS, Icons.Default.Calculate, "Planner"),
        NavItem(AppNavTab.SETTINGS, Icons.Default.Settings, "Config")
    )

    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars),
        containerColor = SurfaceDark,
        tonalElevation = 8.dp
    ) {
        navItems.forEach { item ->
            val isSelected = currentTab == item.tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelect(item.tab) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.size(20.dp)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = BackgroundNavy,
                    selectedTextColor = GoldBright,
                    indicatorColor = GoldPrimary,
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted
                )
            )
        }
    }
}

private data class NavItem(
    val tab: AppNavTab,
    val icon: ImageVector,
    val label: String
)
