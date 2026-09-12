package com.example.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ShellDestinationTest {

    @Test
    fun primaryBottomNavDestinations_areAllNonNullAndValid() {
        val destinations = ShellDestination.primaryBottomNavDestinations

        assertEquals(5, destinations.size)
        destinations.forEach { dest ->
            assertNotNull("Destination in primaryBottomNavDestinations must not be null", dest)
            assertTrue("Route must not be empty", dest.route.isNotEmpty())
            assertTrue("Must be flagged as primary bottom nav", dest.isPrimaryBottomNav)
            assertNotNull("Selected icon must not be null", dest.selectedIcon)
            assertNotNull("Unselected icon must not be null", dest.unselectedIcon)
        }

        val expectedRoutes = listOf(
            "shell_home",
            "shell_exams",
            "shell_practice",
            "shell_mock_tests",
            "shell_more"
        )
        assertEquals(expectedRoutes, destinations.map { it.route })
    }

    @Test
    fun homeDestination_isProperlyConfigured() {
        assertEquals("shell_home", ShellDestination.Home.route)
        assertTrue(ShellDestination.Home.isPrimaryBottomNav)
    }

    @Test
    fun secondaryDestinations_areNotPrimaryBottomNav() {
        assertFalse(ShellDestination.WrongQuestions.isPrimaryBottomNav)
        assertFalse(ShellDestination.Bookmarks.isPrimaryBottomNav)
        assertFalse(ShellDestination.Performance.isPrimaryBottomNav)
    }
}
