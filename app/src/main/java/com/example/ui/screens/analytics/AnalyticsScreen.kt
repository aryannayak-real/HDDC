package com.example.ui.screens.analytics

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
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.PlayerSnapshotEntity
import com.example.data.local.entity.TrackedClanEntity
import com.example.data.local.entity.TrackedPlayerEntity
import com.example.ui.components.ChartPoint
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatMetricCard
import com.example.ui.components.TownHallBadge
import com.example.ui.components.TownHallDistributionChart
import com.example.ui.components.TrophyHistoryLineChart
import com.example.ui.theme.BackgroundNavy
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CyanHUD
import com.example.ui.theme.ElixirPink
import com.example.ui.theme.GemGreen
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AnalyticsScreen(
    trackedPlayers: List<TrackedPlayerEntity>,
    trackedClans: List<TrackedClanEntity>,
    recentSnapshots: List<PlayerSnapshotEntity>,
    modifier: Modifier = Modifier
) {
    var comparePlayer1Tag by remember { mutableStateOf<String?>(null) }
    var comparePlayer2Tag by remember { mutableStateOf<String?>(null) }

    // Defaults
    if (comparePlayer1Tag == null && trackedPlayers.isNotEmpty()) {
        comparePlayer1Tag = trackedPlayers[0].tag
    }
    if (comparePlayer2Tag == null && trackedPlayers.size > 1) {
        comparePlayer2Tag = trackedPlayers[1].tag
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundNavy)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "HDDC CLASH ANALYTICS & INTELLIGENCE",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = GoldBright,
                letterSpacing = 1.sp
            )
        }

        // Town Hall Distribution
        item {
            val thCounts = mutableMapOf<Int, Int>()
            trackedPlayers.forEach { p ->
                thCounts[p.townHallLevel] = (thCounts[p.townHallLevel] ?: 0) + 1
            }
            TownHallDistributionChart(
                distribution = thCounts,
                totalPlayers = trackedPlayers.size
            )
        }

        // Aggregate Clan War Win Rate
        item {
            val totalWins = trackedClans.sumOf { it.warWins }
            val totalClans = trackedClans.size
            val highestStreak = trackedClans.maxOfOrNull { it.warWinStreak } ?: 0

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceCard, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Text(
                    text = "CLAN WAR RECORD (COLLECTED DATA)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatMetricCard(
                        title = "Total Clan Wins",
                        value = "$totalWins",
                        subtitle = "Across $totalClans Clans",
                        accentColor = GoldBright,
                        modifier = Modifier.weight(1f)
                    )
                    StatMetricCard(
                        title = "Best Win Streak",
                        value = "$highestStreak",
                        subtitle = "Current Clan Streak",
                        accentColor = GemGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Head-to-Head Player Comparison
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceCard, RoundedCornerShape(14.dp))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "HEAD-TO-HEAD PLAYER COMPARISON",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldBright,
                        letterSpacing = 1.sp
                    )
                    Icon(imageVector = Icons.AutoMirrored.Filled.CompareArrows, contentDescription = null, tint = GoldPrimary)
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (trackedPlayers.size < 2) {
                    Text(
                        text = "Track at least 2 players to compare combat stats side-by-side.",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                } else {
                    val p1 = trackedPlayers.find { it.tag == comparePlayer1Tag } ?: trackedPlayers[0]
                    val p2 = trackedPlayers.find { it.tag == comparePlayer2Tag } ?: trackedPlayers[1]

                    // Player Selection Pills
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Player 1:", fontSize = 10.sp, color = TextMuted)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                items(trackedPlayers) { p ->
                                    val isSel = p.tag == p1.tag
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSel) CyanHUD else SurfaceCardElevated)
                                            .clickable { comparePlayer1Tag = p.tag }
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = p.name,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSel) BackgroundNavy else TextPrimary
                                        )
                                    }
                                }
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Player 2:", fontSize = 10.sp, color = TextMuted)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                items(trackedPlayers) { p ->
                                    val isSel = p.tag == p2.tag
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSel) GoldBright else SurfaceCardElevated)
                                            .clickable { comparePlayer2Tag = p.tag }
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = p.name,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSel) BackgroundNavy else TextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Names Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = p1.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CyanHUD)
                            Text(text = "TH ${p1.townHallLevel}", fontSize = 11.sp, color = TextMuted)
                        }
                        Text(text = "VS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = p2.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GoldBright)
                            Text(text = "TH ${p2.townHallLevel}", fontSize = 11.sp, color = TextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    ComparisonMetricRow(title = "Trophies", v1 = p1.trophies, v2 = p2.trophies, unit = "🏆")
                    ComparisonMetricRow(title = "Best Trophies", v1 = p1.bestTrophies, v2 = p2.bestTrophies, unit = "🏆")
                    ComparisonMetricRow(title = "War Stars", v1 = p1.warStars, v2 = p2.warStars, unit = "★")
                    ComparisonMetricRow(title = "Attack Wins", v1 = p1.attackWins, v2 = p2.attackWins)
                    ComparisonMetricRow(title = "Donations Sent", v1 = p1.donations, v2 = p2.donations)
                    ComparisonMetricRow(title = "Donations Recv", v1 = p1.donationsReceived, v2 = p2.donationsReceived)
                }
            }
        }

        // Recent Trophy Snapshots Line Chart
        item {
            val chartPoints = recentSnapshots.map {
                ChartPoint(timestamp = it.timestamp, value = it.trophies.toFloat())
            }
            TrophyHistoryLineChart(points = chartPoints)
        }
    }
}

@Composable
private fun ComparisonMetricRow(
    title: String,
    v1: Int,
    v2: Int,
    unit: String = ""
) {
    val p1Wins = v1 > v2
    val p2Wins = v2 > v1

    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "$v1 $unit",
                fontSize = 12.sp,
                fontWeight = if (p1Wins) FontWeight.Bold else FontWeight.Normal,
                color = if (p1Wins) CyanHUD else TextMuted
            )
            Text(text = title, fontSize = 11.sp, color = TextSecondary)
            Text(
                text = "$v2 $unit",
                fontSize = 12.sp,
                fontWeight = if (p2Wins) FontWeight.Bold else FontWeight.Normal,
                color = if (p2Wins) GoldBright else TextMuted
            )
        }
    }
}
