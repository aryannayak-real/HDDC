package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CyanHUD
import com.example.ui.theme.ElixirPink
import com.example.ui.theme.GemGreen
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ChartPoint(val timestamp: Long, val value: Float, val label: String = "")

@Composable
fun TrophyHistoryLineChart(
    points: List<ChartPoint>,
    modifier: Modifier = Modifier,
    lineColor: Color = GoldBright,
    fillStartColor: Color = GoldPrimary.copy(alpha = 0.35f)
) {
    if (points.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(SurfaceCard, RoundedCornerShape(12.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No snapshots recorded yet. Refresh player to record tracking snapshots.",
                color = TextMuted,
                fontSize = 13.sp
            )
        }
        return
    }

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(points) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, animationSpec = tween(700))
    }

    val minVal = (points.minOfOrNull { it.value } ?: 0f) - 50f
    val maxVal = (points.maxOfOrNull { it.value } ?: 100f) + 50f
    val valueRange = (maxVal - minVal).coerceAtLeast(1f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceCard, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TROPHY PROGRESSION (${points.size} SNAPSHOTS)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
            Text(
                text = "Latest: ${points.last().value.toInt()} 🏆",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = GoldBright
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            val width = size.width
            val height = size.height
            val padBottom = 20f
            val padTop = 10f
            val usableHeight = height - padBottom - padTop

            // Gridlines
            val gridCount = 3
            for (i in 0..gridCount) {
                val y = padTop + usableHeight * (i.toFloat() / gridCount)
                drawLine(
                    color = BorderSubtle,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1f
                )
            }

            if (points.size == 1) {
                val single = points.first()
                val cy = padTop + usableHeight * (1f - ((single.value - minVal) / valueRange))
                drawCircle(color = lineColor, radius = 6.dp.toPx(), center = Offset(width / 2f, cy))
                return@Canvas
            }

            val strokePath = Path()
            val fillPath = Path()

            val stepX = width / (points.size - 1)

            val coords = points.mapIndexed { index, point ->
                val x = index * stepX
                val normalizedY = ((point.value - minVal) / valueRange).coerceIn(0f, 1f)
                val targetY = padTop + usableHeight * (1f - normalizedY)
                val animatedY = height - (height - targetY) * animProgress.value
                Offset(x, animatedY)
            }

            strokePath.moveTo(coords[0].x, coords[0].y)
            fillPath.moveTo(coords[0].x, height)
            fillPath.lineTo(coords[0].x, coords[0].y)

            for (i in 0 until coords.size - 1) {
                val p0 = coords[i]
                val p1 = coords[i + 1]
                val midX = (p0.x + p1.x) / 2f
                strokePath.cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
                fillPath.cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
            }

            fillPath.lineTo(coords.last().x, height)
            fillPath.close()

            // Draw Area Fill
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(fillStartColor, Color.Transparent),
                    startY = padTop,
                    endY = height
                )
            )

            // Draw Curve Line
            drawPath(
                path = strokePath,
                color = lineColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // Draw Point Circles
            coords.forEach { pt ->
                drawCircle(color = SurfaceCard, radius = 5.dp.toPx(), center = pt)
                drawCircle(color = lineColor, radius = 3.dp.toPx(), center = pt)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Time labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val sdf = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
            Text(
                text = sdf.format(Date(points.first().timestamp)),
                fontSize = 10.sp,
                color = TextMuted
            )
            Text(
                text = sdf.format(Date(points.last().timestamp)),
                fontSize = 10.sp,
                color = TextMuted
            )
        }
    }
}

@Composable
fun TownHallDistributionChart(
    distribution: Map<Int, Int>,
    totalPlayers: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceCard, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Text(
            text = "TOWN HALL LEVEL BREAKDOWN",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (distribution.isEmpty() || totalPlayers == 0) {
            Text(
                text = "No players available for Town Hall distribution.",
                color = TextMuted,
                fontSize = 13.sp
            )
            return
        }

        val sorted = distribution.entries.sortedByDescending { it.key }
        val maxCount = sorted.maxOfOrNull { it.value } ?: 1

        sorted.forEach { (th, count) ->
            val percent = (count.toFloat() / totalPlayers * 100).toInt()
            val ratio = (count.toFloat() / maxCount).coerceIn(0.05f, 1f)

            val barColor = when {
                th >= 16 -> CyanHUD
                th >= 14 -> GoldBright
                th >= 12 -> ElixirPink
                th >= 9 -> GemGreen
                else -> Color(0xFF64748B)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TH $th",
                    modifier = Modifier.width(44.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(18.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(BorderSubtle)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(ratio)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(barColor)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "$count ($percent%)",
                    modifier = Modifier.width(60.dp),
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun DonationHealthCard(
    donations: Int,
    received: Int,
    modifier: Modifier = Modifier
) {
    val total = (donations + received).coerceAtLeast(1)
    val donationShare = donations.toFloat() / total
    val ratio = if (received == 0) donations.toDouble() else donations.toDouble() / received

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceCard, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "DONATION ACTIVITY",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
            Text(
                text = "Ratio: %.2f".format(ratio),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (ratio >= 1.0) GemGreen else if (ratio >= 0.5) GoldBright else ElixirPink
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Dual Progress Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(ElixirPink.copy(alpha = 0.4f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(donationShare)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(5.dp))
                    .background(GemGreen)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(GemGreen)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Sent: $donations", fontSize = 12.sp, color = TextPrimary)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(ElixirPink)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Received: $received", fontSize = 12.sp, color = TextPrimary)
            }
        }
    }
}
