package com.learningblueprint.student.ui.quiz

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learningblueprint.core.model.*
import com.learningblueprint.core.theme.*
import com.learningblueprint.student.sync.StudentSyncManager

@Composable
fun PracticeQuizScreen(
    exam: Exam,
    paper: ExamPaper,
    subject: Subject,
    chapter: Chapter,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current

    // DYNAMIC DATA BINDING: Query cached questions with fallback to built-in catalog
    val questions = remember(chapter.id) {
        val cached = StudentSyncManager.getCachedConfig(context).questions.filter { it.chapterId == chapter.id }
        if (cached.isNotEmpty()) cached else Question.getQuestionsForChapter(chapter.id)
    }

    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    val userSelectedAnswers = remember { mutableStateMapOf<Int, Int>() } // questionIndex -> optionIndex
    var showQuizSummary by remember { mutableStateOf(false) }

    val totalQuestions = questions.size
    val answeredCount = userSelectedAnswers.size
    val correctCount = questions.indices.count { idx ->
        userSelectedAnswers[idx] == questions[idx].correctOptionIndex
    }
    val wrongCount = answeredCount - correctCount

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF041C16), DeepGreenDark, Color(0xFF02130F))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            // ================= TOP APP BAR =================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0x22FFFFFF))
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PaperLight,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "${subject.code} • CH ${chapter.chapterNumber}",
                            color = SaffronYellow,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = chapter.titleHindi,
                            color = PaperLight,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Live Score Pill
                if (totalQuestions > 0 && !showQuizSummary) {
                    Surface(
                        shape = RoundedCornerShape(99.dp),
                        color = Color(0x24FFFFFF),
                        border = BorderStroke(1.dp, SaffronYellow.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "✓ $correctCount",
                                color = Color(0xFF2EE68B),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "•",
                                color = Color(0x66FFFFFF),
                                fontSize = 10.sp
                            )
                            Text(
                                text = "✗ $wrongCount",
                                color = Color(0xFFFF8B80),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (totalQuestions == 0) {
                // ================= EMPTY STATE =================
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0x14FFFFFF),
                    border = BorderStroke(1.dp, Color(0x28FFFFFF)),
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "इस अध्याय में अभी कोई प्रश्न उपलब्ध नहीं हैं",
                            color = PaperLight,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "गुरु जी (Admin App) में जाकर 'बल्क पेस्ट' द्वारा इस अध्याय में 30 से 150 प्रश्न आयात करें और पब्लिश करें।",
                            color = Color(0xFF8FC3B4),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            } else if (showQuizSummary) {
                // ================= QUIZ RESULT SUMMARY =================
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0x20FFFFFF)),
                    border = BorderStroke(1.5.dp, SaffronYellow),
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "अभ्यास सारांश • SUMMARY",
                            color = SaffronYellow,
                            fontFamily = RozhaOneFontFamily,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${chapter.titleHindi} (${chapter.importanceBadge})",
                            color = Color(0xFF8FC3B4),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("$totalQuestions", color = PaperLight, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                                Text("कुल प्रश्न", color = Color(0xFF8FC3B4), fontSize = 11.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("$correctCount", color = Color(0xFF2EE68B), fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                                Text("सही उत्तर", color = Color(0xFF2EE68B), fontSize = 11.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("$wrongCount", color = Color(0xFFFF8B80), fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                                Text("गलत उत्तर", color = Color(0xFFFF8B80), fontSize = 11.sp)
                            }
                        }

                        val accuracy = if (totalQuestions > 0) (correctCount * 100) / totalQuestions else 0
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "सटीकता दर (Accuracy): $accuracy%",
                            color = SaffronYellow,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = {
                                    userSelectedAnswers.clear()
                                    currentQuestionIndex = 0
                                    showQuizSummary = false
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Retry", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("पुनः अभ्यास", color = PaperLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = onBackClick,
                                colors = ButtonDefaults.buttonColors(containerColor = SaffronYellow, contentColor = DeepGreenDark),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("अध्याय सूची पर लौटें", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                // ================= ACTIVE QUESTION VIEW =================
                val question = questions[currentQuestionIndex]
                val selectedOption = userSelectedAnswers[currentQuestionIndex]
                val isAnswered = selectedOption != null

                // Progress Bar
                LinearProgressIndicator(
                    progress = { (currentQuestionIndex + 1).toFloat() / totalQuestions.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = SaffronYellow,
                    trackColor = Color(0x22FFFFFF)
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Question Header Card
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0x1CFFFFFF),
                            border = BorderStroke(1.dp, Color(0x33FFFFFF)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = SaffronYellow.copy(alpha = 0.25f),
                                        border = BorderStroke(0.8.dp, SaffronYellow)
                                    ) {
                                        Text(
                                            text = "प्रश्न ${currentQuestionIndex + 1} / $totalQuestions",
                                            color = SaffronYellow,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }

                                    Text(
                                        text = question.examYearText,
                                        color = Color(0xFF8FC3B4),
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = question.questionHindi,
                                    color = PaperLight,
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 22.sp
                                )

                                if (question.questionEnglish.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = question.questionEnglish,
                                        color = Color(0xFFB5C9C0),
                                        fontSize = 12.sp,
                                        lineHeight = 17.sp
                                    )
                                }
                            }
                        }
                    }

                    // 4 Interactive Options
                    items(question.options.size) { optIdx ->
                        val optText = question.options[optIdx]
                        val isThisSelected = selectedOption == optIdx
                        val isCorrectOption = question.correctOptionIndex == optIdx

                        val (cardColor, borderColor, textColor) = when {
                            !isAnswered -> Triple(Color(0x14FFFFFF), Color(0x28FFFFFF), PaperLight)
                            isCorrectOption -> Triple(Color(0x2E1C9E5F), Color(0xFF2EE68B), Color(0xFFE8F8F0))
                            isThisSelected && !isCorrectOption -> Triple(Color(0x2EDD4F3A), Color(0xFFFF6B60), Color(0xFFFDEDEB))
                            else -> Triple(Color(0x0CFFFFFF), Color(0x18FFFFFF), Color(0x88FFFFFF))
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = cardColor,
                            border = BorderStroke(1.2.dp, borderColor),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !isAnswered) {
                                    userSelectedAnswers[currentQuestionIndex] = optIdx
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isAnswered && isCorrectOption) Color(0xFF2EE68B) else if (isAnswered && isThisSelected) Color(0xFFFF6B60) else Color(0x24FFFFFF),
                                    modifier = Modifier.size(26.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        if (isAnswered && isCorrectOption) {
                                            Icon(Icons.Default.Check, contentDescription = "Correct", tint = DeepGreenDark, modifier = Modifier.size(16.dp))
                                        } else if (isAnswered && isThisSelected) {
                                            Icon(Icons.Default.Close, contentDescription = "Wrong", tint = Color.White, modifier = Modifier.size(16.dp))
                                        } else {
                                            Text(
                                                text = "(${optIdx + 1})",
                                                color = PaperLight,
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = optText,
                                    color = textColor,
                                    fontSize = 13.sp,
                                    fontWeight = if (isThisSelected || (isAnswered && isCorrectOption)) FontWeight.Bold else FontWeight.Medium,
                                    lineHeight = 19.sp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Rich Pedagogical Explanation Card (Revealed after answering)
                    if (isAnswered && question.explanationHindi.isNotBlank()) {
                        item {
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn() + slideInVertically()
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color(0xFF062B21),
                                    border = BorderStroke(1.2.dp, SaffronYellow.copy(alpha = 0.8f)),
                                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = "💡 विस्तृत पेडागॉजिकल विश्लेषण",
                                                color = SaffronYellow,
                                                fontSize = 13.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = question.explanationHindi,
                                            color = Color(0xFFE2EFE8),
                                            fontSize = 12.sp,
                                            lineHeight = 18.5.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // ================= BOTTOM BAR CONTROLS =================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            if (currentQuestionIndex > 0) currentQuestionIndex--
                        },
                        enabled = currentQuestionIndex > 0,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Prev", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("पिछला", fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    if (currentQuestionIndex < totalQuestions - 1) {
                        Button(
                            onClick = { currentQuestionIndex++ },
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronYellow, contentColor = DeepGreenDark),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("अगला प्रश्न", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next", modifier = Modifier.size(14.dp))
                        }
                    } else {
                        Button(
                            onClick = { showQuizSummary = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C9E5F), contentColor = PaperLight),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("परिणाम देखें", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
