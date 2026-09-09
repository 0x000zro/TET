package com.example.ui.feature.modules

/**
 * Clean architectural placeholders representing future module targets.
 * As mandated by Step 1, educational functionality is NOT implemented here;
 * rather, modular contract boundaries are defined so that each feature module
 * (Home, Exams, Syllabus, Practice, Mock Tests, PYQ, eBooks, Videos, Bookmarks,
 * Wrong Questions, Performance, Settings) can plug cleanly into the architecture in Step 2.
 */
sealed interface ModularFeatureContract {
    val featureId: String
    val displayName: String

    object HomeModule : ModularFeatureContract {
        override val featureId = "module_home"
        override val displayName = "Home & Dashboard"
    }

    object ExamsModule : ModularFeatureContract {
        override val featureId = "module_exams"
        override val displayName = "Exams & Tests"
    }

    object SyllabusModule : ModularFeatureContract {
        override val featureId = "module_syllabus"
        override val displayName = "Curriculum & Syllabus"
    }

    object PracticeModule : ModularFeatureContract {
        override val featureId = "module_practice"
        override val displayName = "Practice Engine"
    }

    object MockTestsModule : ModularFeatureContract {
        override val featureId = "module_mock_tests"
        override val displayName = "Mock Tests Simulation"
    }

    object PreviousYearQuestionsModule : ModularFeatureContract {
        override val featureId = "module_pyq"
        override val displayName = "Previous Year Questions"
    }

    object EBooksModule : ModularFeatureContract {
        override val featureId = "module_ebooks"
        override val displayName = "eBooks & Textbooks"
    }

    object VideosModule : ModularFeatureContract {
        override val featureId = "module_videos"
        override val displayName = "Video Lectures"
    }

    object BookmarksModule : ModularFeatureContract {
        override val featureId = "module_bookmarks"
        override val displayName = "Saved & Bookmarks"
    }

    object WrongQuestionsModule : ModularFeatureContract {
        override val featureId = "module_wrong_questions"
        override val displayName = "Mistake Notebook"
    }

    object PerformanceModule : ModularFeatureContract {
        override val featureId = "module_performance"
        override val displayName = "Performance Analytics"
    }

    object SettingsModule : ModularFeatureContract {
        override val featureId = "module_settings"
        override val displayName = "Settings & Preferences"
    }
}
