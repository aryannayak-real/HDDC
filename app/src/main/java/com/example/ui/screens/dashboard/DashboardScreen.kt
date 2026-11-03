package com.example.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.PlayerSnapshotEntity
import com.example.data.local.entity.TrackedClanEntity
import com.example.data.local.entity.TrackedPlayerEntity
import com.example.ui.AppNavTab
import com.example.ui.components.ClanBadgeView
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatMetricCard
import com.example.ui.components.TownHallBadge
import com.example.ui.theme.BackgroundNavy
import com.example.ui.theme.BorderActive
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CyanHUD
import com.example.ui.theme.GemGreen
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    trackedPlayers: List<TrackedPlayerEntity>,
    trackedClans: List<TrackedClanEntity>,
    recentSnapshots: List<PlayerSnapshotEntity>,
    onSearchPlayer: (String) -> Unit,
    onSearchClan: (String) -> Unit,
    onSelectPlayer: (String) -> Unit,
    onSelectClan: (String) -> Unit,
    onNavigateTab: (AppNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundNavy),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Quick Search Section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceCard, RoundedCornerShape(14.dp))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "QUICK INTELLIGENCE LOOKUP",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldBright,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it.uppercase() },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter Player or Clan Tag (e.g. #2PP)", color = TextMuted, fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = GoldPrimary)
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        focusManager.clearFocus()
                        if (searchQuery.isNotBlank()) onSearchPlayer(searchQuery)
                    }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceCardElevated,
                        unfocusedContainerColor = SurfaceCardElevated,
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            if (searchQuery.isNotBlank()) onSearchPlayer(searchQuery)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldPrimary,
                            contentColor = BackgroundNavy
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Search Player", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            if (searchQuery.isNotBlank()) onSearchClan(searchQuery)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SurfaceCardElevated,
                            contentColor = TextPrimary
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderActive),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Groups, contentDescription = null, modifier = Modifier.size(16.dp), tint = CyanHUD)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Search Clan", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Summary Statistics Grid
        item {
            val maxTrophies = trackedPlayers.maxOfOrNull { it.trophies } ?: 0
            val totalWarsWon = trackedClans.sumOf { it.warWins }

            Column {
                Text(
                    text = "HDDC INTELLIGENCE OVERVIEW",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatMetricCard(
                        title = "Tracked Players",
                        value = "${trackedPlayers.size}",
                        subtitle = if (trackedPlayers.isNotEmpty()) "Top: $maxTrophies 🏆" else "None saved",
                        icon = Icons.Default.Person,
                        accentColor = GoldBright,
                        modifier = Modifier.weight(1f)
                    )
                    StatMetricCard(
                        title = "Tracked Clans",
                        value = "${trackedClans.size}",
                        subtitle = if (trackedClans.isNotEmpty()) "$totalWarsWon Total War Wins" else "None saved",
                        icon = Icons.Default.Shield,
                        accentColor = CyanHUD,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Feature Quick Navigation
        item {
            Column {
                Text(
                    text = "INTELLIGENCE MODULES",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ModuleQuickButton(
                        title = "War Center",
                        icon = Icons.Default.MilitaryTech,
                        accent = GoldPrimary,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateTab(AppNavTab.WAR) }
                    )
                    ModuleQuickButton(
                        title = "Analytics",
                        icon = Icons.Default.Insights,
                        accent = CyanHUD,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateTab(AppNavTab.ANALYTICS) }
                    )
                    ModuleQuickButton(
                        title = "Calculators",
                        icon = Icons.Default.Calculate,
                        accent = GemGreen,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateTab(AppNavTab.CALCULATORS) }
                    )
                    ModuleQuickButton(
                        title = "Settings",
                        icon = Icons.Default.Settings,
                        accent = TextSecondary,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateTab(AppNavTab.SETTINGS) }
                    )
                }
            }
        }

        // Tracked Players Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TRACKED PLAYERS (${trackedPlayers.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
                if (trackedPlayers.isNotEmpty()) {
                    Text(
                        text = "View All",
                        fontSize = 12.sp,
                        color = GoldBright,
                        modifier = Modifier.clickable { onNavigateTab(AppNavTab.PLAYERS) }
                    )
                }
            }
        }

        if (trackedPlayers.isEmpty()) {
            item {
                EmptyStateView(
                    icon = Icons.Default.Person,
                    title = "No Tracked Players Yet",
                    message = "Search a player by tag above to view their live army, heroes, trophies and begin tracking snapshots.",
                    buttonText = "Open Player Intelligence",
                    onButtonClick = { onNavigateTab(AppNavTab.PLAYERS) }
                )
            }
        } else {
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(trackedPlayers) { player ->
                        TrackedPlayerCard(
                            player = player,
                            onClick = { onSelectPlayer(player.tag) }
                        )
                    }
                }
            }
        }

        // Tracked Clans Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TRACKED CLANS (${trackedClans.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
                if (trackedClans.isNotEmpty()) {
                    Text(
                        text = "View All",
                        fontSize = 12.sp,
                        color = GoldBright,
                        modifier = Modifier.clickable { onNavigateTab(AppNavTab.CLANS) }
                    )
                }
            }
        }

        if (trackedClans.isEmpty()) {
            item {
                EmptyStateView(
                    icon = Icons.Default.Shield,
                    title = "No Tracked Clans",
                    message = "Search your clan or rivals to inspect rosters, war streaks, and clan capital stats.",
                    buttonText = "Open Clan Intelligence",
                    onButtonClick = { onNavigateTab(AppNavTab.CLANS) }
                )
            }
        } else {
            items(trackedClans) { clan ->
                TrackedClanRow(
                    clan = clan,
                    onClick = { onSelectClan(clan.tag) }
                )
            }
        }

        // Recent Tracking Activity
        if (recentSnapshots.isNotEmpty()) {
            item {
                Text(
                    text = "RECENT TRACKING SNAPSHOTS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
            }
            items(recentSnapshots.take(5)) { snap ->
                val sdf = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceCard, RoundedCornerShape(8.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = snap.playerTag,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldBright
                        )
                        Text(
                            text = sdf.format(Date(snap.timestamp)),
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${snap.trophies} 🏆",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "▲ ${snap.donations}",
                            fontSize = 12.sp,
                            color = GemGreen
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModuleQuickButton(
    title: String,
    icon: ImageVector,
    accent: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(SurfaceCard, RoundedCornerShape(10.dp))
            .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = icon, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = title, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
    }
}

@Composable
private fun TrackedPlayerCard(
    player: TrackedPlayerEntity,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(180.dp)
            .background(SurfaceCard, RoundedCornerShape(12.dp))
            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TownHallBadge(level = player.townHallLevel)
                Text(text = "${player.trophies} 🏆", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GoldBright)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = player.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1
            )
            Text(
                text = player.tag,
                fontSize = 11.sp,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(6.dp))

            if (!player.clanName.isNullOrBlank()) {
                Text(
                    text = "Clan: ${player.clanName}",
                    fontSize = 11.sp,
                    color = CyanHUD,
                    maxLines = 1
                )
            } else {
                Text(
                    text = "No Clan",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }
    }
}

@Composable
private fun TrackedClanRow(
    clan: TrackedClanEntity,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceCard, RoundedCornerShape(12.dp))
            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ClanBadgeView(url = clan.badgeUrl, modifier = Modifier.size(46.dp))
        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = clan.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(text = "${clan.tag} • Level ${clan.clanLevel}", fontSize = 12.sp, color = TextMuted)
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(text = "${clan.clanPoints} 🏆", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GoldBright)
            Text(text = "${clan.membersCount}/50 members", fontSize = 11.sp, color = TextSecondary)
        }
    }
}
