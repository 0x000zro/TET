package com.example.data.local.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.datasource.DefaultLocalEducationalDataSource
import com.example.data.local.db.entity.AppStateEntity
import com.example.data.local.db.entity.ContentSyncStateEntity
import com.example.data.local.db.entity.LocalPreferenceEntity
import com.example.data.repository.EducationalRepositoryImpl
import com.example.domain.model.AppState
import com.example.domain.model.ContentSyncState
import com.example.domain.model.SyncStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AppDatabaseTest {

    private lateinit var database: AppDatabase
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        DatabaseProvider.setTestInstance(database)
    }

    @After
    fun tearDown() {
        database.close()
        DatabaseProvider.setTestInstance(null)
    }

    @Test
    fun databaseCreation_isSuccessfulAndOpen() {
        // Initialize underlying SQLite instance
        database.openHelper.writableDatabase
        assertTrue(database.isOpen)
        assertNotNull(database.appStateDao())
        assertNotNull(database.localPreferenceDao())
        assertNotNull(database.contentSyncStateDao())
    }

    @Test
    fun appStateDao_insertReadUpdateFlow() = runBlocking {
        val dao = database.appStateDao()

        // 1. Initial query should be null
        val initial = dao.getAppState()
        assertNull(initial)

        // 2. Insert AppState
        val state1 = AppStateEntity(
            isFirstLaunch = true,
            isContentInitialized = false,
            lastKnownContentVersion = 1,
            schemaVersion = 1,
            isCompatible = true,
            lastLaunchTimestamp = 1000L,
            updatedAtTimestamp = 1000L
        )
        dao.insertOrUpdateAppState(state1)

        val retrieved1 = dao.getAppState()
        assertNotNull(retrieved1)
        assertEquals(true, retrieved1?.isFirstLaunch)
        assertEquals(false, retrieved1?.isContentInitialized)

        // 3. Flow observation
        val observedState = dao.getAppStateFlow().first()
        assertNotNull(observedState)
        assertEquals(true, observedState?.isFirstLaunch)

        // 4. Update AppState
        val state2 = state1.copy(
            isFirstLaunch = false,
            isContentInitialized = true,
            lastKnownContentVersion = 2,
            updatedAtTimestamp = 2000L
        )
        dao.insertOrUpdateAppState(state2)

        val retrieved2 = dao.getAppState()
        assertNotNull(retrieved2)
        assertEquals(false, retrieved2?.isFirstLaunch)
        assertEquals(true, retrieved2?.isContentInitialized)
        assertEquals(2, retrieved2?.lastKnownContentVersion)

        // 5. Delete
        dao.deleteAppState()
        assertNull(dao.getAppState())
    }

    @Test
    fun localPreferenceDao_insertReadUpdateDelete() = runBlocking {
        val dao = database.localPreferenceDao()

        // 1. Insert preferences
        val pref1 = LocalPreferenceEntity(preferenceKey = "target_exam", preferenceValue = "JEE_ADVANCED")
        val pref2 = LocalPreferenceEntity(preferenceKey = "theme_mode", preferenceValue = "DARK")
        dao.insertOrUpdatePreference(pref1)
        dao.insertOrUpdatePreference(pref2)

        // 2. Read single preference
        val value1 = dao.getPreferenceValue("target_exam")
        assertEquals("JEE_ADVANCED", value1)

        // 3. Flow observation of all preferences
        val allPrefs = dao.getAllPreferencesFlow().first()
        assertEquals(2, allPrefs.size)

        // 4. Update preference
        val updatedPref = pref1.copy(preferenceValue = "NEET_UG")
        dao.insertOrUpdatePreference(updatedPref)
        assertEquals("NEET_UG", dao.getPreferenceValue("target_exam"))

        // 5. Delete single preference
        dao.deletePreference("theme_mode")
        assertNull(dao.getPreferenceValue("theme_mode"))
        assertEquals(1, dao.getAllPreferencesFlow().first().size)

        // 6. Clear all preferences
        dao.clearAllPreferences()
        assertTrue(dao.getAllPreferencesFlow().first().isEmpty())
    }

    @Test
    fun contentSyncStateDao_insertReadUpdateDelete() = runBlocking {
        val dao = database.contentSyncStateDao()

        // 1. Insert sync states
        val syllabusSync = ContentSyncStateEntity(
            contentSource = "syllabus_v1",
            contentVersion = 1,
            lastSuccessfulSyncTimestamp = 5000L,
            syncStatus = "SUCCESS"
        )
        dao.insertOrUpdateSyncState(syllabusSync)

        // 2. Read
        val retrieved = dao.getSyncState("syllabus_v1")
        assertNotNull(retrieved)
        assertEquals("syllabus_v1", retrieved?.contentSource)
        assertEquals("SUCCESS", retrieved?.syncStatus)

        // 3. Flow observation
        val observed = dao.getSyncStateFlow("syllabus_v1").first()
        assertEquals(1, observed?.contentVersion)

        // 4. Update
        val updated = syllabusSync.copy(
            contentVersion = 2,
            syncStatus = "SYNCING"
        )
        dao.insertOrUpdateSyncState(updated)
        val retrievedUpdated = dao.getSyncState("syllabus_v1")
        assertEquals(2, retrievedUpdated?.contentVersion)
        assertEquals("SYNCING", retrievedUpdated?.syncStatus)

        // 5. Delete
        dao.deleteSyncState("syllabus_v1")
        assertNull(dao.getSyncState("syllabus_v1"))
    }

    @Test
    fun repositoryIntegration_observesAndPersistsThroughRoom() = runBlocking {
        val dataSource = DefaultLocalEducationalDataSource(databaseProvider = { database })
        val repository = EducationalRepositoryImpl(
            localDataSource = dataSource,
            ioDispatcher = Dispatchers.Unconfined
        )

        // 1. Initial app state observation returns default AppState
        val initialAppState = repository.observeAppState().first()
        assertTrue(initialAppState.isFirstLaunch)

        // 2. Save new AppState via repository
        val newState = AppState(
            isFirstLaunch = false,
            isContentInitialized = true,
            lastKnownContentVersion = 3
        )
        val saveResult = repository.saveAppState(newState)
        assertTrue(saveResult.isSuccess)

        // 3. Verify updated state through repository
        val updatedAppState = repository.getAppState()
        assertNotNull(updatedAppState)
        assertFalse(updatedAppState!!.isFirstLaunch)
        assertTrue(updatedAppState.isContentInitialized)
        assertEquals(3, updatedAppState.lastKnownContentVersion)

        // 4. Test preference persistence via repository
        val prefResult = repository.savePreference("student_goal_minutes", "60")
        assertTrue(prefResult.isSuccess)
        assertEquals("60", repository.getPreferenceValue("student_goal_minutes"))

        // 5. Test sync state persistence via repository
        val syncState = ContentSyncState(
            contentSource = "offline_catalog",
            contentVersion = 1,
            syncStatus = SyncStatus.SUCCESS
        )
        val syncResult = repository.saveSyncState(syncState)
        assertTrue(syncResult.isSuccess)

        val observedSyncState = repository.observeSyncState("offline_catalog").first()
        assertNotNull(observedSyncState)
        assertEquals(SyncStatus.SUCCESS, observedSyncState?.syncStatus)
    }
}
