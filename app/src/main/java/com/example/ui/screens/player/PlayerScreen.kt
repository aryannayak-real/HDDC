package com.example.ui.screens.player

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.PlayerSnapshotEntity
import com.example.data.local.entity.TrackedPlayerEntity
import com.example.data.model.PlayerDetail
import com.example.data.model.UnitItem
import com.example.ui.components.ChartPoint
import com.example.ui.components.ClanBadgeView
import com.example.ui.components.DonationHealthCard
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatMetricCard
import com.example.ui.components.TownHallBadge
import com.example.ui.components.TrophyHistoryLineChart
import com.example.ui.theme.BackgroundNavy
import com.example.ui.theme.BorderActive
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CyanHUD
import com.example.ui.theme.DarkElixirPurple
import com.example.ui.theme.ElixirPink
import com.example.ui.theme.GemGreen
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlayerScreen(
    player: PlayerDetail?,
    trackedPlayers: List<TrackedPlayerEntity>,
    snapshots: List<PlayerSnapshotEntity>,
    isLoading: Boolean,
    onSearch: (String) -> Unit,
    onToggleTrack: (PlayerDetail) -> Unit,
    onRefresh: () -> Unit,
    onInspectClan: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchInput by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Overview, 1: Armies & Equipment, 2: Analytics & History, 3: Achievements
    val focusManager = LocalFocusManager.current

    val isTracked = player != null && trackedPlayers.any { it.tag == player.tag }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundNavy)
    ) {
        // Search Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchInput,
                onValueChange = { searchInput = it.uppercase() },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Player Tag (e.g. #2PP)", color = TextMuted, fontSize = 13.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    focusManager.clearFocus()
                    if (searchInput.isNotBlank()) onSearch(searchInput)
                }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceCard,
                    unfocusedContainerColor = SurfaceCard,
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = BorderSubtle,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    focusManager.clearFocus()
                    if (searchInput.isNotBlank()) onSearch(searchInput)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldPrimary,
                    contentColor = BackgroundNavy
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = BackgroundNavy, strokeWidth = 2.dp)
                } else {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                }
            }
        }

        if (player == null) {
            EmptyStateView(
                icon = Icons.Default.MilitaryTech,
                title = "No Player Selected",
                message = "Search a player tag or select one of your tracked players to inspect detailed armies, heroes, and trophy progression.",
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Player Top Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .background(SurfaceCard, RoundedCornerShape(14.dp))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = player.name,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            TownHallBadge(level = player.townHallLevel, weaponLevel = player.townHallWeaponLevel)
                        }
                        Text(
                            text = "${player.tag} • Level ${player.expLevel}",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }

                    Row {
                        IconButton(onClick = onRefresh) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh", tint = CyanHUD)
                        }
                        IconButton(onClick = { onToggleTrack(player) }) {
                            Icon(
                                imageVector = if (isTracked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Track",
                                tint = if (isTracked) GoldPrimary else TextMuted
                            )
                        }
                    }
                }

                // Clan row if available
                if (player.clan != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceCardElevated)
                            .clickable { onInspectClan(player.clan.tag) }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ClanBadgeView(url = player.clan.badgeUrls?.small, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = player.clan.name,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Role: ${player.role?.replaceFirstChar { it.uppercase() } ?: "Member"}",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                        Text(
                            text = "View Clan →",
                            fontSize = 11.sp,
                            color = GoldBright
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tab Bar
            val tabs = listOf("Overview", "Armies", "History", "Achievements")
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = BackgroundNavy,
                contentColor = GoldPrimary,
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = GoldPrimary
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) GoldBright else TextMuted
                            )
                        }
                    )
                }
            }

            // Tab Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        // Overview Tab: Stats & Metrics
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                StatMetricCard(
                                    title = "Trophies",
                                    value = "${player.trophies}",
                                    subtitle = "Best: ${player.bestTrophies}",
                                    accentColor = GoldBright,
                                    modifier = Modifier.weight(1f)
                                )
                                StatMetricCard(
                                    title = "War Stars",
                                    value = "${player.warStars}",
                                    subtitle = "Preference: ${player.warPreference ?: "Unknown"}",
                                    accentColor = CyanHUD,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                StatMetricCard(
                                    title = "Attack Wins",
                                    value = "${player.attackWins}",
                                    subtitle = "Season Attacks",
                                    accentColor = GemGreen,
                                    modifier = Modifier.weight(1f)
                                )
                                StatMetricCard(
                                    title = "Defense Wins",
                                    value = "${player.defenseWins}",
                                    subtitle = "Season Defenses",
                                    accentColor = ElixirPink,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        if (player.builderHallLevel != null) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    StatMetricCard(
                                        title = "Builder Base",
                                        value = "BH ${player.builderHallLevel}",
                                        subtitle = "${player.builderBaseTrophies} 🏆 (Best: ${player.bestBuilderBaseTrophies})",
                                        accentColor = DarkElixirPurple,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        item {
                            DonationHealthCard(
                                donations = player.donations,
                                received = player.donationsReceived
                            )
                        }

                        // Heroes Quick Preview
                        if (player.heroes.isNotEmpty()) {
                            item {
                                Text(
                                    text = "ROYAL HEROES (${player.heroes.size})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary,
                                    letterSpacing = 1.sp
                                )
                            }
                            item {
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    player.heroes.forEach { hero ->
                                        UnitBadge(item = hero, accentColor = GoldBright)
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        // Armies & Equipment Tab
                        if (player.heroes.isNotEmpty()) {
                            item {
                                Text(
                                    text = "HEROES",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldBright,
                                    letterSpacing = 1.sp
                                )
                            }
                            item {
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    player.heroes.forEach { UnitBadge(item = it, accentColor = GoldBright) }
                                }
                            }
                        }

                        if (player.heroEquipment.isNotEmpty()) {
                            item {
                                Text(
                                    text = "HERO EQUIPMENT",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyanHUD,
                                    letterSpacing = 1.sp
                                )
                            }
                            item {
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    player.heroEquipment.forEach { UnitBadge(item = it, accentColor = CyanHUD) }
                                }
                            }
                        }

                        if (player.troops.isNotEmpty()) {
                            item {
                                Text(
                                    text = "TROOPS & SIEGE MACHINES",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ElixirPink,
                                    letterSpacing = 1.sp
                                )
                            }
                            item {
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    player.troops.forEach { UnitBadge(item = it, accentColor = ElixirPink) }
                                }
                            }
                        }

                        if (player.spells.isNotEmpty()) {
                            item {
                                Text(
                                    text = "SPELLS",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkElixirPurple,
                                    letterSpacing = 1.sp
                                )
                            }
                            item {
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    player.spells.forEach { UnitBadge(item = it, accentColor = DarkElixirPurple) }
                                }
                            }
                        }
                    }

                    2 -> {
                        // History & Analytics Tab
                        item {
                            val chartPoints = snapshots.map {
                                ChartPoint(timestamp = it.timestamp, value = it.trophies.toFloat())
                            }
                            TrophyHistoryLineChart(points = chartPoints)
                        }

                        item {
                            Text(
                                text = "HDDC SNAPSHOT HISTORY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                letterSpacing = 1.sp
                            )
                        }

                        if (snapshots.isEmpty()) {
                            item {
                                Text(
                                    text = "No snapshots recorded yet. Refresh this player while tracking to record progression data.",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            }
                        } else {
                            items(snapshots.reversed()) { snap ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(SurfaceCard, RoundedCornerShape(8.dp))
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(text = "${snap.trophies} Trophies 🏆", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GoldBright)
                                        Text(text = "Donations: ${snap.donations} | Recv: ${snap.donationsReceived}", fontSize = 11.sp, color = TextMuted)
                                    }
                                    Text(
                                        text = "${snap.warStars} Stars ★",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CyanHUD
                                    )
                                }
                            }
                        }
                    }

                    3 -> {
                        // Achievements Tab
                        items(player.achievements) { ach ->
                            AchievementCard(achievement = ach)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UnitBadge(
    item: UnitItem,
    accentColor: androidx.compose.ui.graphics.Color
) {
    val isMax = item.level >= item.maxLevel
    Box(
        modifier = Modifier
            .background(SurfaceCard, RoundedCornerShape(8.dp))
            .border(1.dp, if (isMax) accentColor else BorderSubtle, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = item.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isMax) accentColor else SurfaceCardElevated)
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                Text(
                    text = if (isMax) "MAX ${item.level}" else "Lv ${item.level}/${item.maxLevel}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isMax) BackgroundNavy else TextSecondary
                )
            }
        }
    }
}

@Composable
private fun AchievementCard(achievement: com.example.data.model.Achievement) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceCard, RoundedCornerShape(10.dp))
            .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = achievement.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Row {
                    repeat(achievement.stars) {
                        Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = GoldBright, modifier = Modifier.size(14.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = achievement.info,
                fontSize = 11.sp,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(6.dp))

            val progress = if (achievement.target > 0) {
                (achievement.value.toFloat() / achievement.target).coerceIn(0f, 1f)
            } else 1f

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${achievement.value} / ${achievement.target}",
                    fontSize = 10.sp,
                    color = TextSecondary
                )
                if (achievement.completionInfo != null) {
                    Text(
                        text = achievement.completionInfo,
                        fontSize = 10.sp,
                        color = GemGreen
                    )
                }
            }
        }
    }
}
