package com.learningblueprint.student.ui.chapter
import com.learningblueprint.student.sync.StudentSyncManager

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learningblueprint.core.model.Chapter
import com.learningblueprint.core.model.Exam
import com.learningblueprint.core.model.ExamPaper
import com.learningblueprint.core.model.Subject
import com.learningblueprint.core.theme.*

@Composable
fun ChapterRoadmapScreen(
    exam: Exam,
    paper: ExamPaper,
    subject: Subject,
    onBackClick: () -> Unit,
    onChapterClick: (Chapter) -> Unit
) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val allChapters = remember(subject.code) {
        run { val c = StudentSyncManager.getCachedConfig(ctx).chapters.filter { it.subjectCode == subject.code && it.isPublished }; if (c.isNotEmpty()) c else Chapter.getChaptersForSubject(subject.code) }
    }

    var showOnlyHighYield by remember { mutableStateOf(false) }

    val displayedChapters = remember(allChapters, showOnlyHighYield) {
        if (showOnlyHighYield) allChapters.filter { it.isHighYield } else allChapters
    }

    val totalPyqs = remember(allChapters) { allChapters.sumOf { it.pyqCount } }
    val highYieldCount = remember(allChapters) { allChapters.count { it.isHighYield } }
    val subjectAccentColor = Color(subject.hexColor)

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
                        text = "अध्याय एवं PYQ ब्लूप्रिंट",
                        fontFamily = RozhaOneFontFamily,
                        fontSize = 20.sp,
                        color = PaperLight
                    )
                    Text(
                        text = "${subject.code} • ${exam.code} (${paper.label})".uppercase(),
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
                // Analytics Summary Card
                item {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0x22FFFFFF),
                        border = BorderStroke(1.2.dp, subjectAccentColor.copy(alpha = 0.5f)),
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
                                        color = subjectAccentColor
                                    ) {
                                        Text(
                                            text = subject.code,
                                            color = Color.White,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }

                                    Text(
                                        text = subject.titleHindi,
                                        color = PaperLight,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(99.dp),
                                    color = SaffronYellow.copy(alpha = 0.2f),
                                    border = BorderStroke(0.8.dp, SaffronYellow)
                                ) {
                                    Text(
                                        text = "${subject.marks} अंक वेटेज",
                                        color = SaffronYellow,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // 3-Metric Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x18FFFFFF))
                                    .padding(vertical = 8.dp, horizontal = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("कुल अध्याय", color = Color(0xFF8FC3B4), fontSize = 10.sp)
                                    Text("${allChapters.size}", color = PaperLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                                Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color(0x33FFFFFF)))
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("अति-महत्वपूर्ण", color = Color(0xFF8FC3B4), fontSize = 10.sp)
                                    Text("$highYieldCount अध्याय", color = SaffronYellow, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                                Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color(0x33FFFFFF)))
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("PYQ प्रश्न बैंक", color = Color(0xFF8FC3B4), fontSize = 10.sp)
                                    Text("$totalPyqs प्रश्न", color = Color(0xFF1C9E5F), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "✎ विगत 10 वर्षों के PYQ वेटेज अनुसार प्राथमिकता क्रम:",
                                fontFamily = KalamFontFamily,
                                color = Color(0xDDFFE6B0),
                                fontSize = 12.5.sp
                            )
                        }
                    }
                }

                // Filter Chips
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = !showOnlyHighYield,
                            onClick = { showOnlyHighYield = false },
                            label = { Text("सभी अध्याय (${allChapters.size})", fontSize = 11.5.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SaffronYellow,
                                selectedLabelColor = DeepGreenDark,
                                containerColor = Color(0x1AFFFFFF),
                                labelColor = PaperLight
                            ),
                            border = BorderStroke(1.dp, if (!showOnlyHighYield) SaffronYellow else Color(0x33FFFFFF))
                        )

                        FilterChip(
                            selected = showOnlyHighYield,
                            onClick = { showOnlyHighYield = true },
                            label = { Text("★ अति-महत्वपूर्ण केवल ($highYieldCount)", fontSize = 11.5.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VermilionRed,
                                selectedLabelColor = Color.White,
                                containerColor = Color(0x1AFFFFFF),
                                labelColor = PaperLight
                            ),
                            border = BorderStroke(1.dp, if (showOnlyHighYield) VermilionRed else Color(0x33FFFFFF))
                        )
                    }
                }

                // Chapter Cards
                items(displayedChapters, key = { it.id }) { chapter ->
                    ChapterCard(
                        chapter = chapter,
                        subjectAccent = subjectAccentColor,
                        onSolveClick = { onChapterClick(chapter) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ChapterCard(
    chapter: Chapter,
    subjectAccent: Color,
    onSolveClick: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(chapter.isHighYield) }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0x18FFFFFF),
        border = BorderStroke(
            1.2.dp,
            if (chapter.isHighYield) SaffronYellow.copy(alpha = 0.55f) else Color(0x28FFFFFF)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Chapter Number, Importance Badge, PYQ Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (chapter.isHighYield) SaffronYellow else Color(0x28FFFFFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = String.format("%02d", chapter.chapterNumber),
                            color = if (chapter.isHighYield) DeepGreenDark else PaperLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(99.dp),
                        color = if (chapter.isHighYield) VermilionRed.copy(alpha = 0.22f) else Color(0x20FFFFFF),
                        border = BorderStroke(
                            0.8.dp,
                            if (chapter.isHighYield) VermilionRed else Color(0x33FFFFFF)
                        )
                    ) {
                        Text(
                            text = chapter.importanceBadge,
                            color = if (chapter.isHighYield) Color(0xFFFF8B80) else Color(0xFFB5C9C0),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(99.dp),
                    color = Color(0x261C9E5F),
                    border = BorderStroke(0.8.dp, Color(0xFF1C9E5F))
                ) {
                    Text(
                        text = "${chapter.pyqCount} PYQs",
                        color = Color(0xFF8FC3B4),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bilingual Title
            Text(
                text = chapter.titleHindi,
                color = PaperLight,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 20.sp
            )

            Text(
                text = chapter.titleEnglish,
                color = Color(0xFF8FC3B4),
                fontSize = 11.sp,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Weightage Note
            Text(
                text = "📊 ${chapter.weightageText}",
                color = Color(0xFFD4E3DC),
                fontSize = 11.5.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Expandable Key Concepts Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { isExpanded = !isExpanded }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isExpanded) "▼ मुख्य परीक्षा बिंदु छुपाएं" else "▶ मुख्य परीक्षा बिंदु देखें (${chapter.keyConcepts.size})",
                    color = SaffronYellow,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = if (isExpanded) "संक्षिप्त करें" else "विस्तार",
                    color = Color(0xFF8FC3B4),
                    fontSize = 10.sp
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x14000000))
                        .border(0.8.dp, Color(0x22FFFFFF), RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    chapter.keyConcepts.forEach { concept ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "•",
                                color = SaffronYellow,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = concept,
                                color = Color(0xFFCFE0D7),
                                fontSize = 11.5.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Button: Solve PYQ
            Button(
                onClick = onSolveClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (chapter.isHighYield) SaffronYellow else Color(0x28FFFFFF),
                    contentColor = if (chapter.isHighYield) DeepGreenDark else PaperLight
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                modifier = Modifier.fillMaxWidth().height(38.dp)
            ) {
                Text(
                    text = "PYQ प्रश्न हल करें (${chapter.pyqCount})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Solve PYQ",
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
