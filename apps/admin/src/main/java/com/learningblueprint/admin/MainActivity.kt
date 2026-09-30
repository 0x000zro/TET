package com.learningblueprint.admin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.learningblueprint.admin.ui.AdminDashboardScreen
import com.learningblueprint.core.theme.LearningBlueprintTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LearningBlueprintTheme(darkTheme = true) {
                AdminDashboardScreen()
            }
        }
    }
}
