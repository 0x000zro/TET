package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.navigation.ShellDestination
import com.example.ui.feature.shell.MainShellViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("EduPrep", appName)
  }

  @Test
  fun `verify navigation destinations strings`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    assertEquals("Home", context.getString(ShellDestination.Home.titleResId))
    assertEquals("Exams", context.getString(ShellDestination.Exams.titleResId))
    assertEquals("Practice", context.getString(ShellDestination.Practice.titleResId))
    assertEquals("Mock Tests", context.getString(ShellDestination.MockTests.titleResId))
    assertEquals("eBooks", context.getString(ShellDestination.EBooks.titleResId))
    assertEquals("Videos", context.getString(ShellDestination.Videos.titleResId))
    assertEquals("Profile & Settings", context.getString(ShellDestination.ProfileSettings.titleResId))
    assertEquals("Performance", context.getString(ShellDestination.Performance.titleResId))
  }

  @Test
  fun `main shell viewmodel navigates and handles backstack correctly`() {
    val viewModel = MainShellViewModel()

    // Default destination is Home
    assertEquals(ShellDestination.Home, viewModel.uiState.value.currentDestination)
    assertTrue(viewModel.uiState.value.isBottomBarVisible)

    // Navigate to secondary destination eBooks
    viewModel.navigateToDestination(ShellDestination.EBooks)
    assertEquals(ShellDestination.EBooks, viewModel.uiState.value.currentDestination)
    assertFalse(viewModel.uiState.value.isBottomBarVisible)

    // Back returns to Home
    val handled = viewModel.navigateBack()
    assertTrue(handled)
    assertEquals(ShellDestination.Home, viewModel.uiState.value.currentDestination)
    assertTrue(viewModel.uiState.value.isBottomBarVisible)

    // Navigating between primary tabs
    viewModel.navigateToDestination(ShellDestination.MockTests)
    assertEquals(ShellDestination.MockTests, viewModel.uiState.value.currentDestination)
    assertTrue(viewModel.uiState.value.isBottomBarVisible)

    // Back returns to Home root
    val handledPrimaryBack = viewModel.navigateBack()
    assertTrue(handledPrimaryBack)
    assertEquals(ShellDestination.Home, viewModel.uiState.value.currentDestination)
  }
}

