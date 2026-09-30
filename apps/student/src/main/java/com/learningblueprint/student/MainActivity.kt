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
import com.learningblueprint.core.model.Subject
import com.learningblueprint.core.theme.LearningBlueprintTheme
import com.learningblueprint.student.ui.exam.ExamSelectionScreen
import com.learningblueprint.student.ui.subject.SubjectBlueprintScreen
import com.learningblueprint.student.ui.welcome.WelcomeScreen

sealed interface AppScreen {
    data object Welcome : AppScreen
    data object ExamSelection : AppScreen
    data class SubjectBlueprint(val exam: Exam, val paper: ExamPaper) : AppScreen
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            LearningBlueprintTheme(darkTheme = true) {
                var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Welcome) }

                when (val screen = currentScreen) {
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
                                currentScreen = AppScreen.SubjectBlueprint(exam, paper)
                            }
                        )
                    }

                    is AppScreen.SubjectBlueprint -> {
                        BackHandler {
                            currentScreen = AppScreen.ExamSelection
                        }

                        SubjectBlueprintScreen(
                            exam = screen.exam,
                            paper = screen.paper,
                            onBackClick = {
                                currentScreen = AppScreen.ExamSelection
                            },
                            onSubjectSelected = { subject ->
                                Toast.makeText(
                                    this,
                                    "चयनित विषय: ${subject.titleHindi}\nअगला चरण: अध्याय एवं PYQ रोडमैप (Screen 4)!",
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
