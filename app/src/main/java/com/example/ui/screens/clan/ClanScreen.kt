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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import com.example.data.model.CapitalRaidSeason
import com.example.data.model.ClanDetail
import com.example.data.model.ClanMember
import com.example.data.model.WarLogEntry
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
    warLog: List<WarLogEntry> = emptyList(),
    capitalRaids: List<CapitalRaidSeason> = emptyList(),
    modifier: Modifier = Modifier
) {
    var searchInput by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }
    var memberSortBy by remember { mutableStateOf("trophies") }
    var auditFilter by remember { mutableStateOf("all") }
    val focusManager = LocalFocusManager.current

    val isTracked = clan != null && trackedClans.any { it.tag == clan.tag }
    val popularTags = listOf("#2PP", "#8QU8J9LP", "#2Y8G0CC", "#200CC")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundNavy)
    ) {
        // Search Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchInput,
                onValueChange = { searchInput = it.uppercase() },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Clan Tag (e.g. #2PP)", color = TextMuted, fontSize = 13.sp) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BorderActive,
                    unfocusedBorderColor = BorderSubtle,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = SurfaceCard,
                    unfocusedContainerColor = SurfaceCard
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    focusManager.clearFocus()
                    if (searchInput.isNotBlank()) onSearch(searchInput)
                })
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    focusManager.clearFocus()
                    if (searchInput.isNotBlank()) onSearch(searchInput)
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = BackgroundNavy)
            ) {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Search", modifier = Modifier.size(18.dp))
            }
        }

        // Quick Search Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item {
                Text(
                    text = "Quick:",
                    fontSize = 11.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            items(popularTags) { tag ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(SurfaceCardElevated)
                        .clickable {
                            searchInput = tag
                            onSearch(tag)
                        }
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(text = tag, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GoldBright)
                }
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GoldPrimary)
            }
            return
        }

        if (clan == null) {
            EmptyStateView(
                icon = Icons.Default.Shield,
                title = "No Clan Selected",
                message = "Search using any official Clash of Clans clan tag or select one of the quick tags above to inspect roster, war history, and capital stats."
            )
            return
        }

        // Clan Hero Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceCard)
                .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ClanBadgeView(url = clan.badgeUrls?.medium ?: clan.badgeUrls?.small, modifier = Modifier.size(54.dp))
                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = clan.name,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(GoldPrimary.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Lvl ${clan.clanLevel}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldBright
                                )
                            }
                        }

                        Text(
                            text = "${clan.tag} • ${clan.location ?: "International"}",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
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

                // War Center Shortcut Banner
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceCardElevated)
                        .clickable(onClick = onNavigateWar)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.MilitaryTech, contentDescription = null, tint = GoldBright, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "League: ${clan.warLeague ?: "Unranked"} • ${clan.warWinStreak} Win Streak",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Text(text = "War Center →", fontSize = 11.sp, color = GoldBright, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Tabs
        val tabs = listOf(
            "Overview",
            "Roster (${clan.memberList.size})",
            "War Log (${warLog.size})",
            "Capital Raids",
            "Roster Audit"
        )
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
                                title = "Required Trophies",
                                value = "${clan.requiredTrophies}",
                                subtitle = "Entry Requirement",
                                accentColor = DarkElixirPurple,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    if (clan.description.isNotBlank()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SurfaceCard)
                                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                                    .padding(14.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "CLAN DESCRIPTION",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextSecondary,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(text = clan.description, fontSize = 12.sp, color = TextPrimary, lineHeight = 18.sp)
                                }
                            }
                        }
                    }

                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceCard)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                                .padding(14.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "WAR & MEMBERSHIP POLICY",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary,
                                    letterSpacing = 1.sp
                                )
                                PolicyRow("Type", clan.type.replaceFirstChar { it.uppercase() })
                                PolicyRow("War Frequency", clan.warFrequency ?: "Unknown")
                                PolicyRow("War Log", if (clan.isWarLogPublic) "Public" else "Private")
                                PolicyRow("Total Members", "${clan.members} / 50")
                            }
                        }
                    }
                }

                1 -> {
                    // Member Roster Tab
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SORT BY:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                SortChip("Trophies", memberSortBy == "trophies") { memberSortBy = "trophies" }
                                SortChip("Donations", memberSortBy == "donations") { memberSortBy = "donations" }
                                SortChip("Role", memberSortBy == "role") { memberSortBy = "role" }
                                SortChip("Name", memberSortBy == "name") { memberSortBy = "name" }
                            }
                        }
                    }

                    val sortedMembers = when (memberSortBy) {
                        "donations" -> clan.memberList.sortedByDescending { it.donations }
                        "role" -> clan.memberList.sortedBy {
                            when (it.role.lowercase()) {
                                "leader" -> 1
                                "co-leader", "coleader" -> 2
                                "admin", "elder" -> 3
                                else -> 4
                            }
                        }
                        "name" -> clan.memberList.sortedBy { it.name.lowercase() }
                        else -> clan.memberList.sortedByDescending { it.trophies }
                    }

                    items(sortedMembers) { member ->
                        MemberCard(member = member, onClick = { onSelectMember(member.tag) })
                    }
                }

                2 -> {
                    // War Log Tab
                    if (warLog.isEmpty()) {
                        item {
                            EmptyStateView(
                                icon = Icons.Default.MilitaryTech,
                                title = "War Log Unavailable",
                                message = if (!clan.isWarLogPublic) {
                                    "This clan has set its War Log to Private in game settings."
                                } else {
                                    "No recent clan wars returned by the Supercell API for this clan."
                                }
                            )
                        }
                    } else {
                        val wins = warLog.count { it.result?.lowercase() == "win" }
                        val losses = warLog.count { it.result?.lowercase() == "lose" }
                        val ties = warLog.count { it.result?.lowercase() == "tie" }
                        val total = warLog.size
                        val winPct = if (total > 0) (wins.toFloat() / total * 100).toInt() else 0

                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SurfaceCard)
                                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "RECENT WARS RECORD ($total)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GoldBright
                                        )
                                        Text(
                                            text = "$wins Wins • $losses Losses • $ties Ties",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(text = "$winPct%", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = if (winPct >= 65) GemGreen else GoldBright)
                                        Text(text = "Win Rate", fontSize = 10.sp, color = TextMuted)
                                    }
                                }
                            }
                        }

                        items(warLog) { entry ->
                            WarLogItemCard(entry = entry)
                        }
                    }
                }

                3 -> {
                    // Capital Raids Tab
                    if (capitalRaids.isEmpty()) {
                        item {
                            EmptyStateView(
                                icon = Icons.Default.Shield,
                                title = "No Capital Raid Records",
                                message = "Capital raid weekend logs are recorded during weekend battles."
                            )
                        }
                    } else {
                        val latest = capitalRaids.first()
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SurfaceCard)
                                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                                    .padding(14.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "LATEST RAID WEEKEND (${latest.state.uppercase()})",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GoldBright
                                        )
                                        Text(
                                            text = "+${latest.offensiveReward} 🏅 Medals",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CyanHUD
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        StatMetricCard(
                                            title = "Capital Gold Looted",
                                            value = "${latest.capitalTotalLoot}",
                                            subtitle = "${latest.totalAttacks} Attacks Made",
                                            accentColor = GoldBright,
                                            modifier = Modifier.weight(1f)
                                        )
                                        StatMetricCard(
                                            title = "Districts Destroyed",
                                            value = "${latest.enemyDistrictsDestroyed}",
                                            subtitle = "${latest.raidsCompleted} Raids Completed",
                                            accentColor = GemGreen,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Text(
                                text = "MEMBER CONTRIBUTIONS (${latest.members.size})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                letterSpacing = 1.sp
                            )
                        }

                        itemsIndexed(latest.members) { index, m ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SurfaceCard)
                                    .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                                    .clickable { onSelectMember(m.tag) }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(if (index < 3) GoldPrimary.copy(alpha = 0.2f) else SurfaceCardElevated),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "#${index + 1}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (index < 3) GoldBright else TextMuted
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = m.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text(text = "${m.tag} • ${m.attacks}/${m.attackLimit + m.bonusAttackLimit} Attacks", fontSize = 11.sp, color = TextMuted)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${m.capitalResourcesLooted} 🪙",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = GoldBright
                                    )
                                    Text(text = "Capital Gold", fontSize = 9.sp, color = TextMuted)
                                }
                            }
                        }
                    }
                }

                4 -> {
                    // Roster Audit Tab
                    val zeroDonators = clan.memberList.filter { it.donations == 0 }
                    val leechers = clan.memberList.filter { it.donationsReceived > 1000 && it.donations < 100 }
                    val activeDonators = clan.memberList.filter { it.donations > 0 }
                    val compliancePct = if (clan.memberList.isNotEmpty()) {
                        (activeDonators.size.toFloat() / clan.memberList.size * 100).toInt()
                    } else 0

                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceCard)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                                .padding(14.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "CLAN HEALTH & DONATION AUDIT",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldBright
                                    )
                                    Text(
                                        text = "$compliancePct% Active",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (compliancePct >= 70) GemGreen else LossRed
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { compliancePct / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = if (compliancePct >= 70) GemGreen else LossRed,
                                    trackColor = BackgroundNavy
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "${activeDonators.size} Donating", fontSize = 11.sp, color = GemGreen)
                                    Text(text = "${zeroDonators.size} Zero Donators", fontSize = 11.sp, color = if (zeroDonators.isNotEmpty()) LossRed else TextMuted)
                                    Text(text = "${leechers.size} Imbalanced Ratios", fontSize = 11.sp, color = GoldBright)
                                }
                            }
                        }
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            SortChip("All (${clan.memberList.size})", auditFilter == "all") { auditFilter = "all" }
                            SortChip("Zero Donators (${zeroDonators.size})", auditFilter == "zero") { auditFilter = "zero" }
                            SortChip("Ratio Deficit (${leechers.size})", auditFilter == "leechers") { auditFilter = "leechers" }
                        }
                    }

                    val auditedList = when (auditFilter) {
                        "zero" -> zeroDonators
                        "leechers" -> leechers
                        else -> clan.memberList.sortedBy { it.donations }
                    }

                    items(auditedList) { member ->
                        MemberCard(member = member, onClick = { onSelectMember(member.tag) })
                    }
                }
            }
        }
    }
}

@Composable
private fun WarLogItemCard(entry: WarLogEntry) {
    val resultText = entry.result?.uppercase() ?: "COMPLETED"
    val badgeColor = when (entry.result?.lowercase()) {
        "win" -> GemGreen
        "lose" -> LossRed
        "tie" -> GoldBright
        else -> TextMuted
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceCard)
            .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(badgeColor.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = resultText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = badgeColor
                    )
                }

                Text(
                    text = "${entry.teamSize}v${entry.teamSize} War • +${entry.clan.expEarned} XP",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.clan.name ?: "Our Clan",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${entry.clan.stars} ⭐ • ${String.format("%.1f", entry.clan.destructionPercentage)}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldBright
                    )
                }

                Text(text = "VS", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = TextMuted, modifier = Modifier.padding(horizontal = 8.dp))

                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text(
                        text = entry.opponent.name ?: "Opponent",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                    Text(
                        text = "${entry.opponent.stars} ⭐ • ${String.format("%.1f", entry.opponent.destructionPercentage)}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )
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
            .padding(vertical = 4.dp),
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
                color = if (member.donations == 0 && member.donationsReceived > 500) LossRed else TextSecondary
            )
        }
    }
}
