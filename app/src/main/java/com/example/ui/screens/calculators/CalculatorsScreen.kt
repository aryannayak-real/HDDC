package com.example.ui.screens.calculators

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.platform.LocalContext
import com.example.util.NotificationHelper
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
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ActiveBuilderEntity
import com.example.data.model.PlayerDetail
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatMetricCard
import com.example.ui.components.TownHallBadge
import com.example.ui.theme.BackgroundNavy
import com.example.ui.theme.BorderActive
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CyanHUD
import com.example.ui.theme.DarkElixirPurple
import com.example.ui.theme.ElixirPink
import com.example.ui.theme.GemGreen
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.LossRed
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.ClashCalculators
import com.example.util.ClashPlannerData
import com.example.util.InputDeduplicator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CalculatorsScreen(
    selectedPlayer: PlayerDetail? = null,
    activeBuilders: List<ActiveBuilderEntity> = emptyList(),
    onSetBuilder: (slotId: Int, slotName: String, target: String, level: Int, hours: Double) -> Unit = { _, _, _, _, _ -> },
    onClearBuilder: (slotId: Int, slotName: String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        "TH Priority 2026",
        "Village Max Tracker",
        "Builder Scheduler",
        "Equipment & Ores",
        "Upgrade & Walls",
        "Tiebreaker"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundNavy)
    ) {
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    // TH Priority Guide (2026 Meta - Inspired by ClashOS)
                    item { TownHallPriorityGuideSection() }
                }
                1 -> {
                    // Village Max Tracker (Inspired by Clash Max)
                    item { VillageMaxTrackerSection(player = selectedPlayer) }
                }
                2 -> {
                    // Active Builder Scheduler with Staggering Finish analysis
                    item {
                        BuilderSchedulerSection(
                            builders = activeBuilders,
                            onSetBuilder = onSetBuilder,
                            onClearBuilder = onClearBuilder
                        )
                    }
                }
                3 -> {
                    // Hero Equipment & Ore Planner
                    item { EquipmentOrePlannerSection() }
                }
                4 -> {
                    // Upgrade Time & Wall Resource Planner
                    item { UpgradeTimeCalculator() }
                    item { WallResourceCalculator() }
                }
                5 -> {
                    // War Tiebreaker & Donation Health
                    item { WarTiebreakerCalculator() }
                    item { DonationHealthCalculator() }
                }
            }
        }
    }
}

// 1. TOWN HALL PRIORITY GUIDE (2026 META)
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TownHallPriorityGuideSection() {
    var selectedTh by remember { mutableIntStateOf(17) }
    val guide = ClashPlannerData.TOWN_HALL_GUIDES.find { it.thLevel == selectedTh }
        ?: ClashPlannerData.TOWN_HALL_GUIDES.first()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // TH Picker Pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SELECT TOWN HALL",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(listOf(17, 16, 15, 14, 13, 12, 11)) { th ->
                    val isSel = selectedTh == th
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSel) GoldPrimary else SurfaceCard)
                            .border(1.dp, if (isSel) GoldBright else BorderSubtle, RoundedCornerShape(6.dp))
                            .clickable { selectedTh = th }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "TH $th",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSel) BackgroundNavy else TextPrimary
                        )
                    }
                }
            }
        }

        // Guide Header Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceCard, RoundedCornerShape(14.dp))
                .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TownHallBadge(level = guide.thLevel)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = guide.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = guide.strategicSummary, fontSize = 12.sp, color = TextSecondary, lineHeight = 16.sp)

            Spacer(modifier = Modifier.height(10.dp))
            Text(text = "RECOMMENDED WAR ARMIES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GoldBright)
            Spacer(modifier = Modifier.height(4.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                guide.recommendedArmies.forEach { army ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(SurfaceCardElevated)
                            .border(1.dp, BorderActive, RoundedCornerShape(4.dp))
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(text = "⚔ $army", fontSize = 10.sp, color = CyanHUD)
                    }
                }
            }
        }

        // Priority Steps
        Text(
            text = "OPTIMAL BUILDER & LAB SEQUENCE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 1.sp
        )

        guide.steps.forEachIndexed { idx, step ->
            val tagColor = when (step.priorityTag) {
                "Day 1 Mandatory" -> LossRed
                "High Priority" -> GoldBright
                "Strategic" -> CyanHUD
                else -> GemGreen
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceCard, RoundedCornerShape(10.dp))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(SurfaceCardElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "${idx + 1}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GoldBright)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = step.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(tagColor.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(text = step.priorityTag, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = tagColor)
                            }
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(text = step.reason, fontSize = 11.sp, color = TextMuted)
                    }
                }
            }
        }
    }
}

// 2. VILLAGE MAX TRACKER (INSPIRED BY CLASH MAX)
@Composable
private fun VillageMaxTrackerSection(player: PlayerDetail?) {
    if (player == null) {
        EmptyStateView(
            icon = Icons.Default.Person,
            title = "No Tracked Player Selected",
            message = "Select or search a player from the Dashboard or Players tab to inspect their real-time Town Hall completion percentage."
        )
        return
    }

    val progress = ClashPlannerData.evaluatePlayerOffense(player)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Overall Readiness Score
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
                Column {
                    Text(text = player.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(text = "TH ${player.townHallLevel} Offense Maturity", fontSize = 12.sp, color = TextMuted)
                }
                Text(
                    text = "${progress.overallPercent}% MAXED",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (progress.overallPercent >= 80) GemGreen else GoldBright
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { progress.overallPercent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = GoldPrimary,
                trackColor = SurfaceCardElevated
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = progress.statusMessage, fontSize = 12.sp, color = CyanHUD, fontWeight = FontWeight.Medium)
        }

        // Sub-progress Category Breakdown
        ProgressMetricCard(
            category = "Royal Heroes",
            percent = progress.heroesPercent,
            detail = "${progress.heroesRemainingLevels} upgrade levels remaining to TH${player.townHallLevel} cap",
            color = GoldBright
        )

        ProgressMetricCard(
            category = "Laboratory Troops",
            percent = progress.troopsPercent,
            detail = "${progress.troopsRemainingLevels} troop research levels remaining",
            color = ElixirPink
        )

        ProgressMetricCard(
            category = "Laboratory Spells",
            percent = progress.spellsPercent,
            detail = "${progress.spellsRemainingLevels} spell research levels remaining",
            color = DarkElixirPurple
        )

        ProgressMetricCard(
            category = "Hero Equipment (Blacksmith)",
            percent = progress.equipmentPercent,
            detail = "Calculated from current hero equipment levels",
            color = CyanHUD
        )
    }
}

@Composable
private fun ProgressMetricCard(category: String, percent: Int, detail: String, color: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceCard, RoundedCornerShape(10.dp))
            .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = category, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(text = "$percent%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { percent / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = SurfaceCardElevated
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = detail, fontSize = 10.sp, color = TextMuted)
    }
}

// 3. ACTIVE BUILDER SCHEDULER & STAGGERING ENGINE
@Composable
private fun BuilderSchedulerSection(
    builders: List<ActiveBuilderEntity>,
    onSetBuilder: (slotId: Int, slotName: String, target: String, level: Int, hours: Double) -> Unit,
    onClearBuilder: (slotId: Int, slotName: String) -> Unit
) {
    var editSlot by remember { mutableStateOf<ActiveBuilderEntity?>(null) }

    // Staggering Analysis: check if multiple busy builders finish within 6 hours of each other
    val busyBuilders = builders.filter { it.isBusy && it.finishTimeMillis > System.currentTimeMillis() }
    val hasConflict = busyBuilders.size >= 2 && busyBuilders.zipWithNext().any { (a, b) ->
        kotlin.math.abs(a.finishTimeMillis - b.finishTimeMillis) < (6 * 3600 * 1000L)
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Stagger Recommendation Banner (Inspired by ClashOS & Clash Max)
        if (hasConflict) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(LossRed.copy(alpha = 0.15f))
                    .border(1.dp, LossRed, RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = LossRed, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(text = "BUILDER OVERLAP WARNING", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LossRed)
                        Text(
                            text = "Multiple builders finish at the same time. ClashOS recommends staggering builder finish times by 24–36 hours to avoid overflowing storages and prevent loot loss from raids.",
                            fontSize = 11.sp,
                            color = TextPrimary
                        )
                    }
                }
            }
        } else if (busyBuilders.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(GemGreen.copy(alpha = 0.15f))
                    .border(1.dp, GemGreen, RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = GemGreen, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Optimal Stagger: Builder completions are spaced out nicely for continuous farming.",
                        fontSize = 11.sp,
                        color = GemGreen,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Text(
            text = "ACTIVE VILLAGE WORKERS (${builders.size} SLOTS)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 1.sp
        )

        builders.forEach { b ->
            val isFinished = b.isBusy && b.finishTimeMillis <= System.currentTimeMillis()
            val remainingMillis = (b.finishTimeMillis - System.currentTimeMillis()).coerceAtLeast(0L)
            val remainingHours = (remainingMillis / (1000 * 3600)).toInt()
            val remainingDays = remainingHours / 24
            val leftoverHours = remainingHours % 24

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceCard, RoundedCornerShape(10.dp))
                    .border(1.dp, if (b.isBusy) GoldPrimary else BorderSubtle, RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = b.slotName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GoldBright)
                            Spacer(modifier = Modifier.width(8.dp))
                            if (b.isBusy) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isFinished) GemGreen else CyanHUD.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (isFinished) "COMPLETED" else "BUSY",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isFinished) BackgroundNavy else CyanHUD
                                    )
                                }
                            } else {
                                Text(text = "IDLE", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (b.isBusy) {
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "${b.upgradeTarget} (Lv ${b.targetLevel})",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            if (!isFinished) {
                                Text(
                                    text = "Ready in: ${remainingDays}d ${leftoverHours}h",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    Row {
                        IconButton(onClick = { editSlot = b }, modifier = Modifier.size(28.dp)) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = CyanHUD, modifier = Modifier.size(16.dp))
                        }
                        if (b.isBusy) {
                            IconButton(onClick = { onClearBuilder(b.slotId, b.slotName) }, modifier = Modifier.size(28.dp)) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = LossRed, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        val context = LocalContext.current
        Button(
            onClick = {
                NotificationHelper.sendBuilderFinishAlert(
                    context = context,
                    notificationId = 101,
                    slotName = "Builder 1",
                    upgradeTarget = "Town Hall Giga Inferno",
                    targetLevel = 5
                )
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = SurfaceCardElevated),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(imageVector = Icons.Default.Notifications, contentDescription = null, tint = GoldBright, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Send Builder Finish Alert (Test)", fontSize = 11.sp, color = TextPrimary)
        }
    }

    // Set Builder Upgrade Dialog
    if (editSlot != null) {
        val slot = editSlot!!
        var targetName by remember { mutableStateOf(if (slot.upgradeTarget != "Idle") slot.upgradeTarget else "") }
        var targetLevelStr by remember { mutableStateOf(if (slot.targetLevel > 0) slot.targetLevel.toString() else "1") }
        var daysStr by remember { mutableStateOf(if (slot.durationHours > 0) (slot.durationHours / 24).toInt().toString() else "7") }
        var hoursStr by remember { mutableStateOf(if (slot.durationHours > 0) (slot.durationHours % 24).toInt().toString() else "0") }

        AlertDialog(
            onDismissRequest = { editSlot = null },
            title = { Text("Schedule ${slot.slotName}", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = targetName,
                        onValueChange = { targetName = InputDeduplicator.filter(targetName, it) },
                        label = { Text("Building / Troop Name (e.g. Archer Queen)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                    )
                    OutlinedTextField(
                        value = targetLevelStr,
                        onValueChange = { targetLevelStr = InputDeduplicator.filter(targetLevelStr, it) },
                        label = { Text("Target Level") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = daysStr,
                            onValueChange = { daysStr = InputDeduplicator.filter(daysStr, it) },
                            label = { Text("Days") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                        )
                        OutlinedTextField(
                            value = hoursStr,
                            onValueChange = { hoursStr = InputDeduplicator.filter(hoursStr, it) },
                            label = { Text("Hours") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val lvl = targetLevelStr.toIntOrNull() ?: 1
                        val days = daysStr.toDoubleOrNull() ?: 0.0
                        val hrs = hoursStr.toDoubleOrNull() ?: 0.0
                        val totalHours = days * 24.0 + hrs
                        onSetBuilder(slot.slotId, slot.slotName, targetName, lvl, totalHours)
                        editSlot = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = BackgroundNavy)
                ) {
                    Text("Start Timer", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { editSlot = null }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = SurfaceCard
        )
    }
}

// 4. HERO EQUIPMENT & ORE ECONOMY PLANNER
@Composable
private fun EquipmentOrePlannerSection() {
    var selectedEquipIndex by remember { mutableIntStateOf(0) }
    var currentLevelStr by remember { mutableStateOf("1") }
    var targetLevelStr by remember { mutableStateOf("27") }

    val equip = ClashPlannerData.HERO_EQUIPMENTS[selectedEquipIndex]
    val curLvl = currentLevelStr.toIntOrNull() ?: 1
    val tarLvl = targetLevelStr.toIntOrNull() ?: equip.maxLevel

    val oreCost = ClashPlannerData.calculateOreRequirements(curLvl, tarLvl, equip.isEpic)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "HERO EQUIPMENT & ORE ECONOMY",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = GoldBright,
            letterSpacing = 1.sp
        )

        // Equipment Selector Carousel
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(ClashPlannerData.HERO_EQUIPMENTS.indices.toList()) { idx ->
                val eq = ClashPlannerData.HERO_EQUIPMENTS[idx]
                val isSel = selectedEquipIndex == idx
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSel) CyanHUD else SurfaceCard)
                        .border(1.dp, if (isSel) CyanHUD else BorderSubtle, RoundedCornerShape(8.dp))
                        .clickable {
                            selectedEquipIndex = idx
                            targetLevelStr = eq.maxLevel.toString()
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Column {
                        Text(
                            text = eq.name,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSel) BackgroundNavy else TextPrimary
                        )
                        Text(
                            text = if (eq.isEpic) "EPIC (Max ${eq.maxLevel})" else "COMMON (Max ${eq.maxLevel})",
                            fontSize = 9.sp,
                            color = if (isSel) BackgroundNavy else GoldBright
                        )
                    }
                }
            }
        }

        // Selected Equipment Details
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceCard, RoundedCornerShape(12.dp))
                .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(text = "${equip.hero} • ${equip.name}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CyanHUD)
            Text(text = equip.description, fontSize = 11.sp, color = TextSecondary)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = currentLevelStr,
                    onValueChange = { currentLevelStr = InputDeduplicator.filter(currentLevelStr, it) },
                    label = { Text("Current Level") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                )
                OutlinedTextField(
                    value = targetLevelStr,
                    onValueChange = { targetLevelStr = InputDeduplicator.filter(targetLevelStr, it) },
                    label = { Text("Target Level") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                )
            }

            // Ore Requirements Output
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatMetricCard(
                    title = "Shiny Ore",
                    value = "${oreCost.shinyOre}",
                    subtitle = "Common Ore",
                    accentColor = CyanHUD,
                    modifier = Modifier.weight(1f)
                )
                StatMetricCard(
                    title = "Glowy Ore",
                    value = "${oreCost.glowyOre}",
                    subtitle = "Rare Ore",
                    accentColor = ElixirPink,
                    modifier = Modifier.weight(1f)
                )
                if (equip.isEpic) {
                    StatMetricCard(
                        title = "Starry Ore",
                        value = "${oreCost.starryOre}",
                        subtitle = "War-Only Ore",
                        accentColor = GoldBright,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "2026 HERO EQUIPMENT META LOADOUTS & SYNERGIES",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = GoldBright,
            letterSpacing = 1.sp
        )

        ClashPlannerData.META_BUILDS.forEach { meta ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceCard)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${meta.heroName} • ${meta.buildName}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = meta.tier,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = GemGreen
                            )
                        }
                        Button(
                            onClick = {
                                val matchIdx = ClashPlannerData.HERO_EQUIPMENTS.indexOfFirst {
                                    it.name.equals(meta.primaryEquipment, ignoreCase = true)
                                }
                                if (matchIdx >= 0) {
                                    selectedEquipIndex = matchIdx
                                    targetLevelStr = ClashPlannerData.HERO_EQUIPMENTS[matchIdx].maxLevel.toString()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceCardElevated),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Plan Ore →", fontSize = 10.sp, color = CyanHUD, fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(GoldPrimary.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(meta.primaryEquipment, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GoldBright)
                        }
                        Text("+", fontSize = 11.sp, color = TextMuted)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CyanHUD.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(meta.secondaryEquipment, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CyanHUD)
                        }
                    }

                    Text(text = meta.playstyle, fontSize = 11.sp, color = TextSecondary, lineHeight = 16.sp)
                    Text(text = "Power Spike: ${meta.keyPowerSpike}", fontSize = 10.sp, color = GoldBright, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// 5. EXISTING CALCULATORS (UPGRADE TIME, WALLS, WAR TIEBREAKER, DONATIONS)
@Composable
private fun UpgradeTimeCalculator() {
    var baseDaysStr by remember { mutableStateOf("14") }
    var goldPassPercent by remember { mutableIntStateOf(20) }
    var potionsCountStr by remember { mutableStateOf("1") }

    val baseDays = baseDaysStr.toDoubleOrNull() ?: 14.0
    val potions = potionsCountStr.toIntOrNull() ?: 0
    val result = ClashCalculators.calculateUpgradeTime(baseDays, goldPassPercent, potions)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceCard, RoundedCornerShape(14.dp))
            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "BUILDER UPGRADE TIME CALCULATOR",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = GoldBright,
            letterSpacing = 1.sp
        )

        OutlinedTextField(
            value = baseDaysStr,
            onValueChange = { baseDaysStr = InputDeduplicator.filter(baseDaysStr, it) },
            label = { Text("Base Upgrade Time (Days)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
        )

        Column {
            Text(text = "Gold Pass Builder Boost:", fontSize = 11.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(0, 10, 15, 20).forEach { pct ->
                    val isSel = goldPassPercent == pct
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSel) GoldPrimary else SurfaceCardElevated)
                            .clickable { goldPassPercent = pct }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "$pct%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSel) BackgroundNavy else TextPrimary
                        )
                    }
                }
            }
        }

        OutlinedTextField(
            value = potionsCountStr,
            onValueChange = { potionsCountStr = InputDeduplicator.filter(potionsCountStr, it) },
            label = { Text("Builder Potions (Saves 9h each)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatMetricCard(
                title = "Final Duration",
                value = "${result.displayDays}d ${result.displayHours}h",
                subtitle = "Was: ${baseDays.toInt()} Days",
                accentColor = GemGreen,
                modifier = Modifier.weight(1f)
            )
            StatMetricCard(
                title = "Total Time Saved",
                value = "%.1fh".format(result.timeSavedHours),
                subtitle = "Boost + Potions",
                accentColor = GoldBright,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun WallResourceCalculator() {
    var curLvl by remember { mutableIntStateOf(15) }
    var targetLvl by remember { mutableIntStateOf(16) }
    var wallCountStr by remember { mutableStateOf("100") }
    var goldPassPercent by remember { mutableIntStateOf(20) }

    val wallCount = wallCountStr.toIntOrNull() ?: 100
    val result = ClashCalculators.calculateWallCosts(curLvl, targetLvl, wallCount, goldPassPercent)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceCard, RoundedCornerShape(14.dp))
            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "WALL RESOURCE & RING PLANNER",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = GoldBright,
            letterSpacing = 1.sp
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Current Level (TH11-16):", fontSize = 11.sp, color = TextMuted)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(13, 14, 15, 16).forEach { lvl ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (curLvl == lvl) CyanHUD else SurfaceCardElevated)
                                .clickable {
                                    curLvl = lvl
                                    if (targetLvl <= curLvl) targetLvl = curLvl + 1
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(text = "Lv$lvl", fontSize = 11.sp, color = if (curLvl == lvl) BackgroundNavy else TextPrimary)
                        }
                    }
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Target Level:", fontSize = 11.sp, color = TextMuted)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(14, 15, 16, 17).filter { it > curLvl }.forEach { lvl ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (targetLvl == lvl) GoldPrimary else SurfaceCardElevated)
                                .clickable { targetLvl = lvl }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(text = "Lv$lvl", fontSize = 11.sp, color = if (targetLvl == lvl) BackgroundNavy else TextPrimary)
                        }
                    }
                }
            }
        }

        OutlinedTextField(
            value = wallCountStr,
            onValueChange = { wallCountStr = InputDeduplicator.filter(wallCountStr, it) },
            label = { Text("Number of Wall Pieces") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatMetricCard(
                title = "Total Cost",
                value = "%.1fM".format(result.totalCostMillions),
                subtitle = "Gold or Elixir",
                accentColor = ElixirPink,
                modifier = Modifier.weight(1f)
            )
            StatMetricCard(
                title = "Wall Rings",
                value = "${result.totalWallRings}",
                subtitle = "Rings Required",
                accentColor = CyanHUD,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun WarTiebreakerCalculator() {
    var myStarsStr by remember { mutableStateOf("42") }
    var myDestrStr by remember { mutableStateOf("94.5") }
    var oppStarsStr by remember { mutableStateOf("42") }
    var oppDestrStr by remember { mutableStateOf("92.8") }

    val myStars = myStarsStr.toIntOrNull() ?: 0
    val myDestr = myDestrStr.toDoubleOrNull() ?: 0.0
    val oppStars = oppStarsStr.toIntOrNull() ?: 0
    val oppDestr = oppDestrStr.toDoubleOrNull() ?: 0.0

    val outcome = ClashCalculators.analyzeWarOutcome(myStars, myDestr, oppStars, oppDestr)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceCard, RoundedCornerShape(14.dp))
            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "WAR TIEBREAKER & OUTCOME ANALYZER",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = GoldBright,
            letterSpacing = 1.sp
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = myStarsStr,
                onValueChange = { myStarsStr = InputDeduplicator.filter(myStarsStr, it) },
                label = { Text("Your Clan Stars") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
            )
            OutlinedTextField(
                value = myDestrStr,
                onValueChange = { myDestrStr = InputDeduplicator.filter(myDestrStr, it) },
                label = { Text("Your Destr %") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = oppStarsStr,
                onValueChange = { oppStarsStr = InputDeduplicator.filter(oppStarsStr, it) },
                label = { Text("Opponent Stars") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
            )
            OutlinedTextField(
                value = oppDestrStr,
                onValueChange = { oppDestrStr = InputDeduplicator.filter(oppDestrStr, it) },
                label = { Text("Opponent Destr %") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
            )
        }

        val outcomeColor = when (outcome) {
            ClashCalculators.WarOutcome.VICTORY, ClashCalculators.WarOutcome.VICTORY_BY_DESTRUCTION -> GemGreen
            ClashCalculators.WarOutcome.DEFEAT, ClashCalculators.WarOutcome.DEFEAT_BY_DESTRUCTION -> LossRed
            else -> GoldBright
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(outcomeColor.copy(alpha = 0.15f))
                .border(1.dp, outcomeColor, RoundedCornerShape(8.dp))
                .padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = outcome.title.uppercase(), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = outcomeColor)
                Text(text = "Tiebreaker Rule: Stars > Total Destruction % > Duration", fontSize = 10.sp, color = TextMuted)
            }
        }
    }
}

@Composable
private fun DonationHealthCalculator() {
    var sentStr by remember { mutableStateOf("1500") }
    var recvStr by remember { mutableStateOf("600") }

    val sent = sentStr.toIntOrNull() ?: 0
    val recv = recvStr.toIntOrNull() ?: 0
    val rating = ClashCalculators.evaluateDonationRatio(sent, recv)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceCard, RoundedCornerShape(14.dp))
            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "DONATION RATIO & ACTIVITY CALCULATOR",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = GoldBright,
            letterSpacing = 1.sp
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = sentStr,
                onValueChange = { sentStr = InputDeduplicator.filter(sentStr, it) },
                label = { Text("Troops Donated") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
            )
            OutlinedTextField(
                value = recvStr,
                onValueChange = { recvStr = InputDeduplicator.filter(recvStr, it) },
                label = { Text("Troops Received") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatMetricCard(
                title = "Ratio",
                value = "%.2f".format(rating.ratio),
                subtitle = rating.status,
                accentColor = if (rating.isHealthy) GemGreen else LossRed,
                modifier = Modifier.weight(1f)
            )
            StatMetricCard(
                title = "Net Contribution",
                value = "${sent - recv}",
                subtitle = if (sent >= recv) "Donor Surplus" else "Deficit",
                accentColor = if (sent >= recv) GoldBright else ElixirPink,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
