package com.learningblueprint.student

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.learningblueprint.core.model.Chapter
import com.learningblueprint.core.model.Exam
import com.learningblueprint.core.model.ExamPaper
import com.learningblueprint.core.model.Subject
import com.learningblueprint.core.theme.LearningBlueprintTheme
import com.learningblueprint.student.ui.chapter.ChapterRoadmapScreen
import com.learningblueprint.student.ui.exam.ExamSelectionScreen
import com.learningblueprint.student.ui.subject.SubjectBlueprintScreen
import com.learningblueprint.student.ui.welcome.WelcomeScreen

sealed interface AppScreen {
    data object Welcome : AppScreen
    data object ExamSelection : AppScreen
    data class SubjectBlueprint(val exam: Exam, val paper: ExamPaper) : AppScreen
    data class ChapterRoadmap(val exam: Exam, val paper: ExamPaper, val subject: Subject) : AppScreen
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
                                currentScreen = AppScreen.ChapterRoadmap(screen.exam, screen.paper, subject)
                            }
                        )
                    }

                    is AppScreen.ChapterRoadmap -> {
                        BackHandler {
                            currentScreen = AppScreen.SubjectBlueprint(screen.exam, screen.paper)
                        }

                        ChapterRoadmapScreen(
                            exam = screen.exam,
                            paper = screen.paper,
                            subject = screen.subject,
                            onBackClick = {
                                currentScreen = AppScreen.SubjectBlueprint(screen.exam, screen.paper)
                            },
                            onChapterClick = { chapter ->
                                Toast.makeText(
                                    this,
                                    "अध्याय: ${chapter.titleHindi}\nअगला चरण: PYQ प्रश्नोत्तरी व टेस्ट मॉड्यूल (Screen 5)!",
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
