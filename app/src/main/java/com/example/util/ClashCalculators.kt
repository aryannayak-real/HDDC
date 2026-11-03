package com.example.util

import kotlin.math.roundToInt

object ClashCalculators {

    // Town Hall upgrade data (Base days, Gold/Elixir cost in millions)
    data class TownHallTier(
        val level: Int,
        val baseDays: Double,
        val baseCostMillions: Double,
        val maxHeroesLevel: String
    )

    val TOWN_HALL_DATA = listOf(
        TownHallTier(1, 0.001, 0.001, "None"),
        TownHallTier(2, 0.003, 0.001, "None"),
        TownHallTier(3, 0.02, 0.004, "None"),
        TownHallTier(4, 0.04, 0.025, "None"),
        TownHallTier(5, 0.08, 0.15, "None"),
        TownHallTier(6, 0.25, 0.75, "None"),
        TownHallTier(7, 0.5, 1.2, "BK: 5"),
        TownHallTier(8, 1.0, 2.0, "BK: 10"),
        TownHallTier(9, 2.0, 3.0, "BK: 30, AQ: 30"),
        TownHallTier(10, 3.0, 5.0, "BK: 40, AQ: 40"),
        TownHallTier(11, 4.0, 7.0, "BK: 50, AQ: 50, GW: 20"),
        TownHallTier(12, 6.0, 9.5, "BK: 65, AQ: 65, GW: 40"),
        TownHallTier(13, 8.0, 12.0, "BK: 75, AQ: 75, GW: 50, RC: 25"),
        TownHallTier(14, 10.0, 15.0, "BK: 80, AQ: 80, GW: 55, RC: 30"),
        TownHallTier(15, 12.0, 18.0, "BK: 90, AQ: 90, GW: 65, RC: 40"),
        TownHallTier(16, 14.0, 21.0, "BK: 95, AQ: 95, GW: 70, RC: 45"),
        TownHallTier(17, 16.0, 24.0, "BK: 100, AQ: 100, GW: 75, RC: 50, MP: 30")
    )

    // Wall costs per wall piece (in millions Gold/Elixir)
    data class WallTier(val level: Int, val costPerPiece: Double, val wallRings: Int)
    val WALL_DATA = listOf(
        WallTier(11, 0.5, 1),
        WallTier(12, 1.0, 1),
        WallTier(13, 2.0, 2),
        WallTier(14, 3.0, 3),
        WallTier(15, 5.0, 5),
        WallTier(16, 7.0, 7),
        WallTier(17, 9.0, 9)
    )

    /**
     * Calculates reduced upgrade duration with Gold Pass Builder Boost and Builder Potions.
     */
    fun calculateUpgradeTime(
        days: Double,
        goldPassPercent: Int, // 0, 10, 15, 20
        builderPotionsCount: Int = 0
    ): UpgradeTimeResult {
        val discount = 1.0 - (goldPassPercent.coerceIn(0, 20) / 100.0)
        val discountedHours = (days * 24.0) * discount
        // 1 builder potion saves 9 hours per active builder
        val hoursSavedByPotions = builderPotionsCount * 9.0
        val finalHours = (discountedHours - hoursSavedByPotions).coerceAtLeast(0.0)

        val finalDays = finalHours / 24.0
        val remainingHours = (finalHours % 24.0).roundToInt()
        val totalDaysInt = finalDays.toInt()

        return UpgradeTimeResult(
            originalHours = days * 24.0,
            discountedHours = discountedHours,
            finalHours = finalHours,
            displayDays = totalDaysInt,
            displayHours = remainingHours,
            timeSavedHours = (days * 24.0) - finalHours
        )
    }

    data class UpgradeTimeResult(
        val originalHours: Double,
        val discountedHours: Double,
        val finalHours: Double,
        val displayDays: Int,
        val displayHours: Int,
        val timeSavedHours: Double
    )

    /**
     * Calculates wall upgrade requirements for a batch of walls.
     */
    fun calculateWallCosts(
        currentLevel: Int,
        targetLevel: Int,
        wallCount: Int,
        goldPassPercent: Int = 0
    ): WallCostResult {
        val discount = 1.0 - (goldPassPercent.coerceIn(0, 20) / 100.0)
        var totalCostMillions = 0.0
        var totalRings = 0

        for (lvl in (currentLevel + 1)..targetLevel) {
            val tier = WALL_DATA.find { it.level == lvl }
            if (tier != null) {
                totalCostMillions += (tier.costPerPiece * wallCount) * discount
                totalRings += tier.wallRings * wallCount
            }
        }

        return WallCostResult(
            wallCount = wallCount,
            totalCostMillions = totalCostMillions,
            totalWallRings = totalRings
        )
    }

    data class WallCostResult(
        val wallCount: Int,
        val totalCostMillions: Double,
        val totalWallRings: Int
    )

    /**
     * Donation analysis and health rating.
     */
    fun evaluateDonationRatio(donations: Int, received: Int): DonationRating {
        val ratio = if (received == 0) donations.toDouble() else donations.toDouble() / received
        val status = when {
            donations == 0 && received == 0 -> "Inactive"
            ratio >= 2.0 -> "Legendary Donor"
            ratio >= 1.0 -> "Generous Supporter"
            ratio >= 0.5 -> "Balanced"
            ratio >= 0.25 -> "Net Receiver"
            else -> "Warning: Heavy Leech"
        }
        val isHealthy = ratio >= 0.33 || (donations > 500)
        return DonationRating(ratio = ratio, status = status, isHealthy = isHealthy)
    }

    data class DonationRating(
        val ratio: Double,
        val status: String,
        val isHealthy: Boolean
    )

    /**
     * Analyzes war outcome between two sides.
     */
    fun analyzeWarOutcome(
        myStars: Int,
        myDestruction: Double,
        oppStars: Int,
        oppDestruction: Double
    ): WarOutcome {
        return when {
            myStars > oppStars -> WarOutcome.VICTORY
            oppStars > myStars -> WarOutcome.DEFEAT
            myDestruction > oppDestruction -> WarOutcome.VICTORY_BY_DESTRUCTION
            oppDestruction > myDestruction -> WarOutcome.DEFEAT_BY_DESTRUCTION
            else -> WarOutcome.PERFECT_TIE
        }
    }

    enum class WarOutcome(val title: String) {
        VICTORY("Victory (Star Advantage)"),
        DEFEAT("Defeat (Stars Deficit)"),
        VICTORY_BY_DESTRUCTION("Victory (Tiebreaker by Destruction %)"),
        DEFEAT_BY_DESTRUCTION("Defeat (Tiebreaker by Destruction %)"),
        PERFECT_TIE("Perfect Tie")
    }
}
