package com.example.ui.screens.calculators

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.StatMetricCard
import com.example.ui.theme.BackgroundNavy
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CyanHUD
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

@Composable
fun CalculatorsScreen(modifier: Modifier = Modifier) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Upgrade Time, 1: Walls & Resources, 2: War Tiebreaker, 3: Donation Health

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundNavy)
    ) {
        val tabs = listOf("Upgrade Time", "Wall Resources", "War Tiebreaker", "Donation Health")
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
                    // Upgrade Time Calculator
                    item {
                        UpgradeTimeCalculator()
                    }
                }
                1 -> {
                    // Wall & Resource Planner
                    item {
                        WallResourceCalculator()
                    }
                }
                2 -> {
                    // War Tiebreaker Analyzer
                    item {
                        WarTiebreakerCalculator()
                    }
                }
                3 -> {
                    // Donation Health Calculator
                    item {
                        DonationHealthCalculator()
                    }
                }
            }
        }
    }
}

@Composable
private fun UpgradeTimeCalculator() {
    var baseDaysStr by remember { mutableStateOf("14") }
    var goldPassPercent by remember { mutableIntStateOf(20) } // 0, 10, 15, 20
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
            onValueChange = { baseDaysStr = it },
            label = { Text("Base Upgrade Time (Days)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )

        // Gold Pass Builder Boost
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
            onValueChange = { potionsCountStr = it },
            label = { Text("Builder Potions (Saves 9h each)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Result Card
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
            onValueChange = { wallCountStr = it },
            label = { Text("Number of Wall Pieces") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
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
                onValueChange = { myStarsStr = it },
                label = { Text("Your Clan Stars") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
            )
            OutlinedTextField(
                value = myDestrStr,
                onValueChange = { myDestrStr = it },
                label = { Text("Your Destr %") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = oppStarsStr,
                onValueChange = { oppStarsStr = it },
                label = { Text("Opponent Stars") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
            )
            OutlinedTextField(
                value = oppDestrStr,
                onValueChange = { oppDestrStr = it },
                label = { Text("Opponent Destr %") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
            )
        }

        // Outcome Banner
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
                Text(
                    text = outcome.title.uppercase(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = outcomeColor
                )
                Text(
                    text = "Tiebreaker Rule: Stars > Total Destruction % > Duration",
                    fontSize = 10.sp,
                    color = TextMuted
                )
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
                onValueChange = { sentStr = it },
                label = { Text("Troops Donated") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
            )
            OutlinedTextField(
                value = recvStr,
                onValueChange = { recvStr = it },
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
