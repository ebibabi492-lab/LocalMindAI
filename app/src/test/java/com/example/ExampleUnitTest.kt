package com.example

import com.example.ui.AppStrings
import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testLocalizationPersianDetection() {
    assertTrue(AppStrings.isPersian("fa"))
    assertFalse(AppStrings.isPersian("en"))
    assertEquals("LocalMind AI", AppStrings.appName(true))
    assertEquals("LocalMind AI", AppStrings.appName(false))
    assertTrue(AppStrings.welcomeMessage(true).contains("بدون نیاز به اینترنت"))
    assertTrue(AppStrings.noModelMessage(true).contains("دانلود کنید"))
  }
}
