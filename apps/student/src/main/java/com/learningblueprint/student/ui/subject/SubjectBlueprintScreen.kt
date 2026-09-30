package com.learningblueprint.student.ui.subject

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learningblueprint.core.model.Exam
import com.learningblueprint.core.model.ExamPaper
import com.learningblueprint.core.model.Subject
import com.learningblueprint.core.theme.*

@Composable
fun SubjectBlueprintScreen(
    exam: Exam,
    paper: ExamPaper,
    onBackClick: () -> Unit,
    onSubjectSelected: (Subject) -> Unit
) {
    val subjects = remember(exam.id, paper) {
        Subject.getSubjectsForExam(exam.id, paper)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF072922),
                        DeepGreenDark
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // ================= TOP APP BAR =================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0x22FFFFFF))
                        .border(1.dp, Color(0x33FFFFFF), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = PaperLight,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "विषय ब्लूप्रिंट",
                        fontFamily = RozhaOneFontFamily,
                        fontSize = 21.sp,
                        color = PaperLight
                    )
                    Text(
                        text = "${exam.code} • ${paper.label}".uppercase(),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaffronYellow,
                        letterSpacing = 1.2.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // ================= CONTENT LIST =================
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Summary Card
                item {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0x22FFFFFF),
                        border = BorderStroke(1.2.dp, SaffronYellow.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = SaffronYellow
                                    ) {
                                        Text(
                                            text = exam.code,
                                            color = DeepGreenDark,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }

                                    Text(
                                        text = exam.titleHindi,
                                        color = PaperLight,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Text(
                                    text = "कुल ${subjects.size} विषय",
                                    color = SaffronYellow,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x18FFFFFF))
                                    .padding(vertical = 8.dp, horizontal = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("कुल प्रश्न", color = Color(0xFF8FC3B4), fontSize = 10.sp)
                                    Text("${exam.totalQuestions}", color = PaperLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                                Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color(0x33FFFFFF)))
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("पूर्णांक", color = Color(0xFF8FC3B4), fontSize = 10.sp)
                                    Text("${exam.totalMarks} अंक", color = SaffronYellow, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                                Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color(0x33FFFFFF)))
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("समयावधि", color = Color(0xFF8FC3B4), fontSize = 10.sp)
                                    Text("${exam.durationMinutes} मिनट", color = PaperLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "✎ पाठ्यक्रम के अनुसार विषयवार अध्ययन प्रारंभ करें:",
                                    fontFamily = KalamFontFamily,
                                    color = Color(0xDDFFE6B0),
                                    fontSize = 12.5.sp
                                )
                                Text(
                                    text = "नेगेटिव मार्किंग नहीं",
                                    color = Color(0xFF1C9E5F),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Individual Subject Cards
                items(subjects, key = { it.id }) { subject ->
                    SubjectCard(
                        subject = subject,
                        onClick = { onSubjectSelected(subject) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SubjectCard(
    subject: Subject,
    onClick: () -> Unit
) {
    val accentColor = Color(subject.hexColor)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0x18FFFFFF),
        border = BorderStroke(1.2.dp, accentColor.copy(alpha = 0.45f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Code Badge, Title, Marks
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(accentColor.copy(alpha = 0.2f))
                            .border(1.dp, accentColor, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = subject.code.take(4),
                            color = accentColor,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = subject.titleHindi,
                            color = PaperLight,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = subject.titleEnglish,
                            color = Color(0xFF8FC3B4),
                            fontSize = 10.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Weightage Pill
                Surface(
                    shape = RoundedCornerShape(99.dp),
                    color = accentColor.copy(alpha = 0.18f),
                    border = BorderStroke(0.8.dp, accentColor)
                ) {
                    Text(
                        text = "${subject.marks} अंक",
                        color = accentColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Syllabus Description
            Text(
                text = subject.descriptionHindi,
                color = Color(0xFFB9C9C0),
                fontSize = 11.5.sp,
                lineHeight = 16.5.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Row: Chapter count pill and action CTA
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0x22FFFFFF)
                    ) {
                        Text(
                            text = "📚 ${subject.chaptersCount} मुख्य अध्याय",
                            color = Color(0xFFCFE0D7),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0x22FFFFFF)
                    ) {
                        Text(
                            text = "✓ ${subject.questionCount} प्रश्न",
                            color = Color(0xFFCFE0D7),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = "अध्याय ब्लूप्रिंट",
                        color = SaffronYellow,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "View Chapters",
                        tint = SaffronYellow,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
    }
}
