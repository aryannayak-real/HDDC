package com.example

import com.example.data.security.SecurityManager
import com.example.util.ClashCalculators
import com.example.util.TagValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun tagValidator_validatesTagsCorrectly() {
    assertTrue(TagValidator.isValid("#2PP"))
    assertTrue(TagValidator.isValid("2PP"))
    assertTrue(TagValidator.isValid("#8YPVG2Q0"))
    assertFalse(TagValidator.isValid("INVALID-TAG!!"))
    assertEquals("%232PP", TagValidator.urlEncode("#2PP"))
    assertEquals("#2PP", TagValidator.normalize("2pp"))
  }

  @Test
  fun securityManager_encryptsAndDecryptsCredentials() {
    val secret = "supercell-api-token-test-secret"
    val encrypted = SecurityManager.encrypt(secret)
    assertNotEquals(secret, encrypted)
    val decrypted = SecurityManager.decrypt(encrypted)
    assertEquals(secret, decrypted)

    val masked = SecurityManager.mask(secret)
    assertTrue(masked.startsWith("••••"))
    assertTrue(masked.endsWith("ret"))
  }

  @Test
  fun clashCalculators_calculatesUpgradeAndTiebreakers() {
    val upgradeResult = ClashCalculators.calculateUpgradeTime(14.0, 20, 2)
    assertTrue(upgradeResult.timeSavedHours > 0)
    assertTrue(upgradeResult.finalHours < upgradeResult.originalHours)

    val outcomeWin = ClashCalculators.analyzeWarOutcome(45, 98.5, 45, 95.0)
    assertEquals(ClashCalculators.WarOutcome.VICTORY_BY_DESTRUCTION, outcomeWin)

    val wallResult = ClashCalculators.calculateWallCosts(15, 16, 100, 20)
    assertTrue(wallResult.totalCostMillions > 0)
  }
}
