package com.example.ui.screens.war

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import android.widget.Toast
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.TrackedClanEntity
import com.example.data.local.entity.WarAssignmentEntity
import com.example.data.model.CurrentWar
import com.example.data.model.CwlGroup
import com.example.ui.components.ClanBadgeView
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatusBadge
import com.example.ui.theme.BackgroundNavy
import com.example.ui.theme.BorderActive
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CyanHUD
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
fun WarScreen(
    currentWar: CurrentWar?,
    cwlGroup: CwlGroup?,
    trackedClans: List<TrackedClanEntity>,
    warAssignments: List<WarAssignmentEntity>,
    selectedClanTag: String?,
    onSelectClan: (String) -> Unit,
    onRefreshWar: (String) -> Unit,
    onAddAssignment: (clanTag: String, memberTag: String, memberName: String, targetBase: Int, notes: String) -> Unit,
    onUpdateAssignmentStatus: (WarAssignmentEntity, String) -> Unit,
    onDeleteAssignment: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Live War Matchup, 1: Attack Log, 2: War Planner, 3: CWL League
    var showAddDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundNavy)
    ) {
        // Tracked Clan Selector Pill Row
        if (trackedClans.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(trackedClans) { clan ->
                    val isSelected = clan.tag == selectedClanTag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) GoldPrimary else SurfaceCard)
                            .border(1.dp, if (isSelected) GoldBright else BorderSubtle, RoundedCornerShape(8.dp))
                            .clickable {
                                onSelectClan(clan.tag)
                                onRefreshWar(clan.tag)
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${clan.name} (${clan.tag})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) BackgroundNavy else TextPrimary
                        )
                    }
                }
            }
        }

        if (selectedClanTag == null || trackedClans.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.MilitaryTech,
                title = "No Clan Selected for War Tracking",
                message = "Track a clan first from the Clans tab to view live war telemetry, member attacks, and create base attack plans.",
                modifier = Modifier.fillMaxSize()
            )
            return
        }

        // Sub Tabs
        val tabs = listOf("Matchup", "Member Attacks", "War Planner", "CWL League")
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

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            val war = currentWar
            val isNotInWar = war == null || war.state == "notInWar"

            when (selectedTab) {
                0 -> {
                    // Matchup Tab
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StatusBadge(state = war?.state ?: "notInWar")
                            IconButton(onClick = { onRefreshWar(selectedClanTag) }) {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh", tint = CyanHUD)
                            }
                        }
                    }

                    if (isNotInWar || war?.clan == null || war.opponent == null) {
                        item {
                            EmptyStateView(
                                icon = Icons.Default.Shield,
                                title = "Clan Is Not Currently in War",
                                message = "The Supercell API indicates this clan is currently in peace time, or their war log is private."
                            )
                        }
                    } else {
                        // Live War Card
                        item {
                            WarScoreboardCard(war = war)
                        }

                        // Team Stats & Battle Modifier
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SurfaceCard, RoundedCornerShape(12.dp))
                                    .padding(14.dp)
                            ) {
                                Text(
                                    text = "WAR TELEMETRY (SUPERCELL API)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                TelemetryRow("Team Size", "${war.teamSize} vs ${war.teamSize}")
                                TelemetryRow("Attacks Per Member", "${war.attacksPerMember}")
                                if (war.battleModifier != null) {
                                    TelemetryRow("Battle Modifier", war.battleModifier)
                                }
                                if (war.endTime != null) {
                                    TelemetryRow("End Time Timestamp", war.endTime)
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Member Attacks Tab (Actual API records)
                    if (isNotInWar || war?.clan == null) {
                        item {
                            Text(
                                text = "No live war attack data available for this clan.",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        item {
                            Text(
                                text = "MEMBER ATTACK BREAKDOWN (${war.clan.members.size} ROSTER)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                letterSpacing = 1.sp
                            )
                        }

                        items(war.clan.members) { member ->
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
                                            text = "#${member.mapPosition} ${member.name} (TH${member.townhallLevel})",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "${member.attacks.size}/${war.attacksPerMember} Attacks",
                                            fontSize = 11.sp,
                                            color = if (member.attacks.isNotEmpty()) GemGreen else LossRed
                                        )
                                    }

                                    if (member.attacks.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        member.attacks.forEach { att ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 2.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "vs ${att.defenderTag}",
                                                    fontSize = 11.sp,
                                                    color = TextMuted
                                                )
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    repeat(att.stars) {
                                                        Icon(
                                                            imageVector = Icons.Default.Star,
                                                            contentDescription = null,
                                                            tint = GoldBright,
                                                            modifier = Modifier.size(12.dp)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "%.1f%%".format(att.destructionPercentage),
                                                        fontSize = 11.sp,
                                                        color = TextPrimary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // War Planner Tab (HDDC Local feature)
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "HDDC WAR ATTACK PLANNER",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldBright
                                )
                                Text(
                                    text = "Local base assignment and scout notes",
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        val planText = buildString {
                                            appendLine("⚔️ HDDC CLAN WAR PLAN & ORDERS")
                                            if (currentWar?.clan != null && currentWar.opponent != null) {
                                                appendLine("Match: ${currentWar.clan.name} vs ${currentWar.opponent.name}")
                                                appendLine("Score: ${currentWar.clan.stars} ⭐ (${String.format("%.1f", currentWar.clan.destructionPercentage)}%) vs ${currentWar.opponent.stars} ⭐ (${String.format("%.1f", currentWar.opponent.destructionPercentage)}%)")
                                            }
                                            appendLine("─────────────────────────")
                                            appendLine("🎯 TARGET CALLS:")
                                            if (warAssignments.isEmpty()) {
                                                appendLine("No targets assigned yet.")
                                            } else {
                                                warAssignments.sortedBy { it.targetBaseNumber }.forEach { a ->
                                                    appendLine("• Base #${a.targetBaseNumber}: ${a.assignedMemberName} [${a.status}] ${if (a.notes.isNotBlank()) "(${a.notes})" else ""}")
                                                }
                                            }
                                            appendLine("─────────────────────────")
                                            appendLine("Good luck Chiefs! Secure the 3-stars! 🛡️")
                                            appendLine("Generated via HDDC Intelligence Platform")
                                        }
                                        clipboardManager.setText(AnnotatedString(planText))
                                        Toast.makeText(context, "War Plan copied to clipboard for Discord / Chat!", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldBright)
                                ) {
                                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copy Plan", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { showAddDialog = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = GoldPrimary,
                                        contentColor = BackgroundNavy
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Assign", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    if (warAssignments.isEmpty()) {
                        item {
                            EmptyStateView(
                                icon = Icons.Default.MilitaryTech,
                                title = "No Bases Assigned Yet",
                                message = "Use the War Planner to assign clan members to enemy base targets and note attack strategies (e.g. Zap Dragons, Queen Charge)."
                            )
                        }
                    } else {
                        items(warAssignments) { assignment ->
                            WarAssignmentCard(
                                assignment = assignment,
                                onUpdateStatus = { newStatus -> onUpdateAssignmentStatus(assignment, newStatus) },
                                onDelete = { onDeleteAssignment(assignment.id) }
                            )
                        }
                    }
                }

                3 -> {
                    // CWL League Tab
                    if (cwlGroup == null || cwlGroup.state == "notInWar") {
                        item {
                            EmptyStateView(
                                icon = Icons.Default.MilitaryTech,
                                title = "No Active CWL League Group",
                                message = "Clan War Leagues take place once a month during the first week. When active, Supercell API provides round matchups and war tags."
                            )
                        }
                    } else {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SurfaceCard, RoundedCornerShape(12.dp))
                                    .padding(14.dp)
                            ) {
                                Text(
                                    text = "CLAN WAR LEAGUE GROUP (SEASON: ${cwlGroup.season})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldBright,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "State: ${cwlGroup.state.uppercase()} • ${cwlGroup.rounds.size} Rounds Scheduled",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        item {
                            Text(
                                text = "PARTICIPATING CLANS (${cwlGroup.clans.size})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                letterSpacing = 1.sp
                            )
                        }

                        items(cwlGroup.clans) { cwlClan ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SurfaceCard, RoundedCornerShape(10.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ClanBadgeView(url = cwlClan.badgeUrls?.small, modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = cwlClan.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text(text = "${cwlClan.tag} • Level ${cwlClan.clanLevel}", fontSize = 11.sp, color = TextMuted)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Assignment Dialog
    if (showAddDialog && selectedClanTag != null) {
        var memberName by remember { mutableStateOf("") }
        var targetBaseStr by remember { mutableStateOf("1") }
        var notes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Assign Base Target", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = memberName,
                        onValueChange = { memberName = it },
                        label = { Text("Clan Mate Name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    OutlinedTextField(
                        value = targetBaseStr,
                        onValueChange = { targetBaseStr = it },
                        label = { Text("Enemy Base # (e.g. 1-50)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Strategy / Attack Notes") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val baseNum = targetBaseStr.toIntOrNull() ?: 1
                        onAddAssignment(selectedClanTag, "", memberName.ifBlank { "Clan Mate" }, baseNum, notes)
                        showAddDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = BackgroundNavy)
                ) {
                    Text("Save Plan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = SurfaceCard
        )
    }
}

@Composable
private fun WarScoreboardCard(war: CurrentWar) {
    val myClan = war.clan ?: return
    val oppClan = war.opponent ?: return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceCard, RoundedCornerShape(14.dp))
            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // My Clan
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ClanBadgeView(url = myClan.badgeUrls?.medium, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = myClan.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary, maxLines = 1)
                Text(text = "${myClan.stars} ★", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = GoldBright)
                Text(text = "%.1f%%".format(myClan.destructionPercentage), fontSize = 12.sp, color = TextSecondary)
            }

            // VS Divider
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "VS", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GoldPrimary)
                Text(text = "${war.teamSize}v${war.teamSize}", fontSize = 10.sp, color = TextMuted)
            }

            // Opponent Clan
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ClanBadgeView(url = oppClan.badgeUrls?.medium, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = oppClan.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary, maxLines = 1)
                Text(text = "${oppClan.stars} ★", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = LossRed)
                Text(text = "%.1f%%".format(oppClan.destructionPercentage), fontSize = 12.sp, color = TextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Attacks Comparison
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "${myClan.attacks} attacks used", fontSize = 11.sp, color = GemGreen)
            Text(text = "${oppClan.attacks} attacks used", fontSize = 11.sp, color = LossRed)
        }
    }
}

@Composable
private fun TelemetryRow(label: String, value: String) {
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
private fun WarAssignmentCard(
    assignment: WarAssignmentEntity,
    onUpdateStatus: (String) -> Unit,
    onDelete: () -> Unit
) {
    val statusColor = when (assignment.status) {
        "3_STAR" -> GoldBright
        "2_STAR" -> GemGreen
        "1_STAR" -> CyanHUD
        "FAILED" -> LossRed
        else -> TextSecondary
    }

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
                    text = "Target Base #${assignment.targetBaseNumber} → ${assignment.assignedMemberName}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(16.dp))
                }
            }

            if (assignment.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Strategy: ${assignment.notes}", fontSize = 11.sp, color = TextSecondary)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Status Selector Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("PLANNED", "3_STAR", "2_STAR", "FAILED").forEach { status ->
                    val isSelected = assignment.status == status
                    val label = when (status) {
                        "3_STAR" -> "3★"
                        "2_STAR" -> "2★"
                        "FAILED" -> "Fail"
                        else -> "Planned"
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSelected) statusColor else SurfaceCardElevated)
                            .clickable { onUpdateStatus(status) }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) BackgroundNavy else TextMuted
                        )
                    }
                }
            }
        }
    }
}
