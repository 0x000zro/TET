package com.learningblueprint.student

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.learningblueprint.core.model.Exam
import com.learningblueprint.core.model.ExamPaper
import com.learningblueprint.core.theme.LearningBlueprintTheme
import com.learningblueprint.student.ui.exam.ExamSelectionScreen
import com.learningblueprint.student.ui.welcome.WelcomeScreen

sealed interface AppScreen {
    data object Welcome : AppScreen
    data object ExamSelection : AppScreen
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            LearningBlueprintTheme(darkTheme = true) {
                var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Welcome) }
                var selectedExamData by remember { mutableStateOf<Pair<Exam, ExamPaper>?>(null) }

                when (currentScreen) {
                    is AppScreen.Welcome -> {
                        WelcomeScreen(
                            onStartClick = {
                                currentScreen = AppScreen.ExamSelection
                            }
                        )
                    }

                    is AppScreen.ExamSelection -> {
                        BackHandler {
                            currentScreen = AppScreen.Welcome
                        }

                        ExamSelectionScreen(
                            onBackClick = {
                                currentScreen = AppScreen.Welcome
                            },
                            onExamSelected = { exam, paper ->
                                selectedExamData = Pair(exam, paper)
                                Toast.makeText(
                                    this,
                                    "चयनित: ${exam.code} (${paper.label})\nअगला चरण: विषय ब्लूप्रिंट (Screen 3)!",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        )
                    }
                }
            }
        }
    }
}
