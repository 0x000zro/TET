package com.learningblueprint.student

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.learningblueprint.core.theme.LearningBlueprintTheme
import com.learningblueprint.student.ui.welcome.WelcomeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            LearningBlueprintTheme(darkTheme = true) {
                WelcomeScreen(
                    onStartClick = {
                        Toast.makeText(
                            this,
                            "START दबाया गया — अगला कदम: परीक्षा चयन (Screen 2)!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }
        }
    }
}
