package com.example.ui.screens.clan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.data.local.entity.TrackedClanEntity
import com.example.data.model.ClanDetail
import com.example.data.model.ClanMember
import com.example.ui.components.ClanBadgeView
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatMetricCard
import com.example.ui.theme.BackgroundNavy
import com.example.ui.theme.BorderActive
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CyanHUD
import com.example.ui.theme.DarkElixirPurple
import com.example.ui.theme.GemGreen
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.LossRed
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ClanScreen(
    clan: ClanDetail?,
    trackedClans: List<TrackedClanEntity>,
    isLoading: Boolean,
    onSearch: (String) -> Unit,
    onToggleTrack: (ClanDetail) -> Unit,
    onRefresh: () -> Unit,
    onSelectMember: (String) -> Unit,
    onNavigateWar: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchInput by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Overview, 1: Member Roster
    var memberSortBy by remember { mutableStateOf("trophies") } // trophies, donations, name
    val focusManager = LocalFocusManager.current

    val isTracked = clan != null && trackedClans.any { it.tag == clan.tag }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundNavy)
    ) {
        // Search Bar
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
                placeholder = { Text("Clan Tag (e.g. #2PP)", color = TextMuted, fontSize = 13.sp) },
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

        if (clan == null) {
            EmptyStateView(
                icon = Icons.Default.Shield,
                title = "No Clan Selected",
                message = "Search a clan tag above to inspect member rosters, war history, clan capital statistics, and war league standings.",
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Clan Header Banner Card
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ClanBadgeView(url = clan.badgeUrls?.large ?: clan.badgeUrls?.medium, modifier = Modifier.size(54.dp))
                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = clan.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${clan.tag} • Level ${clan.clanLevel} • ${clan.members}/50",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                        if (clan.location != null) {
                            Text(
                                text = "Location: ${clan.location}",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Row {
                        IconButton(onClick = onRefresh) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh", tint = CyanHUD)
                        }
                        IconButton(onClick = { onToggleTrack(clan) }) {
                            Icon(
                                imageVector = if (isTracked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Track",
                                tint = if (isTracked) GoldPrimary else TextMuted
                            )
                        }
                    }
                }

                if (clan.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = clan.description,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp,
                        maxLines = 3
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quick War Shortcut
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceCardElevated)
                        .clickable(onClick = onNavigateWar)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.MilitaryTech, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Check Current War Status",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Text(text = "War Center →", fontSize = 11.sp, color = GoldBright)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tab Selector
            val tabs = listOf("Overview", "Member Roster (${clan.memberList.size})")
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
                        // Overview Tab
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                StatMetricCard(
                                    title = "Clan Points",
                                    value = "${clan.clanPoints}",
                                    subtitle = "Builder: ${clan.clanBuilderBasePoints}",
                                    accentColor = GoldBright,
                                    modifier = Modifier.weight(1f)
                                )
                                StatMetricCard(
                                    title = "War Win Streak",
                                    value = "${clan.warWinStreak}",
                                    subtitle = "${clan.warWins}W / ${clan.warLosses}L / ${clan.warTies}T",
                                    accentColor = GemGreen,
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
                                    title = "Clan Capital",
                                    value = if (clan.capitalHallLevel > 0) "CH ${clan.capitalHallLevel}" else "Capital",
                                    subtitle = "${clan.clanCapitalPoints} Points",
                                    accentColor = CyanHUD,
                                    modifier = Modifier.weight(1f)
                                )
                                StatMetricCard(
                                    title = "War League",
                                    value = clan.warLeague ?: "Unranked",
                                    subtitle = "Freq: ${clan.warFrequency ?: "Always"}",
                                    accentColor = DarkElixirPurple,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SurfaceCard, RoundedCornerShape(12.dp))
                                    .padding(14.dp)
                            ) {
                                Text(
                                    text = "RECRUITMENT & POLICIES",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                PolicyRow("Clan Type", clan.type.replaceFirstChar { it.uppercase() })
                                PolicyRow("Required Trophies", "${clan.requiredTrophies} 🏆")
                                PolicyRow("War Log", if (clan.isWarLogPublic) "Public" else "Private")
                                PolicyRow("Total Members", "${clan.members} / 50")
                            }
                        }
                    }

                    1 -> {
                        // Member Roster Tab with Sorting Controls
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "SORT MEMBERS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary,
                                    letterSpacing = 1.sp
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    SortChip(
                                        label = "Trophies",
                                        isSelected = memberSortBy == "trophies",
                                        onClick = { memberSortBy = "trophies" }
                                    )
                                    SortChip(
                                        label = "Donations",
                                        isSelected = memberSortBy == "donations",
                                        onClick = { memberSortBy = "donations" }
                                    )
                                    SortChip(
                                        label = "Rank",
                                        isSelected = memberSortBy == "rank",
                                        onClick = { memberSortBy = "rank" }
                                    )
                                }
                            }
                        }

                        val sortedMembers = when (memberSortBy) {
                            "donations" -> clan.memberList.sortedByDescending { it.donations }
                            "rank" -> clan.memberList.sortedBy { it.clanRank }
                            else -> clan.memberList.sortedByDescending { it.trophies }
                        }

                        items(sortedMembers) { member ->
                            MemberCard(
                                member = member,
                                onClick = { onSelectMember(member.tag) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PolicyRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = TextMuted)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
    }
}

@Composable
private fun SortChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) GoldPrimary else SurfaceCardElevated)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) BackgroundNavy else TextSecondary
        )
    }
}

@Composable
private fun MemberCard(
    member: ClanMember,
    onClick: () -> Unit
) {
    val roleColor = when (member.role.lowercase()) {
        "leader" -> GoldBright
        "co-leader", "coleader" -> CyanHUD
        "admin", "elder" -> GemGreen
        else -> TextMuted
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceCard, RoundedCornerShape(10.dp))
            .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank Indicator
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(SurfaceCardElevated),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${member.clanRank}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = member.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(roleColor.copy(alpha = 0.15f))
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = member.role.uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = roleColor
                    )
                }
            }

            Text(
                text = "${member.tag} • Level ${member.expLevel}",
                fontSize = 11.sp,
                color = TextMuted
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${member.trophies} 🏆",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = GoldBright
            )
            Text(
                text = "▲ ${member.donations} / ▼ ${member.donationsReceived}",
                fontSize = 10.sp,
                color = TextSecondary
            )
        }
    }
}
