package com.learningblueprint.student.ui.exam

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
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
import com.learningblueprint.core.model.Exam
import com.learningblueprint.core.model.ExamPaper
import com.learningblueprint.core.theme.*

@Composable
fun ExamSelectionScreen(
    onBackClick: () -> Unit,
    onExamSelected: (Exam, ExamPaper) -> Unit
) {
    var selectedExamId by remember { mutableStateOf("ctet") }
    var selectedPaper by remember { mutableStateOf(ExamPaper.BOTH) }

    val currentExam = remember(selectedExamId) {
        Exam.ALL_EXAMS.firstOrNull { it.id == selectedExamId } ?: Exam.ALL_EXAMS.first()
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
            // ================= TOP BAR =================
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
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = PaperLight,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "लक्ष्य परीक्षा चुनें",
                        fontFamily = RozhaOneFontFamily,
                        fontSize = 21.sp,
                        color = PaperLight
                    )
                    Text(
                        text = "SELECT YOUR TARGET EXAM • STEP 1",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaffronYellow,
                        letterSpacing = 1.5.sp
                    )
                }
            }

            // ================= EXAM LIST & OPTIONS =================
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "उपलब्ध शिक्षक पात्रता परीक्षाएं:",
                            color = Color(0xFF8FC3B4),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "✎ आधिकारिक पाठ्यक्रम अनुसार",
                            fontFamily = KalamFontFamily,
                            color = Color(0xAAFFE6B0),
                            fontSize = 12.sp
                        )
                    }
                }

                items(Exam.ALL_EXAMS, key = { it.id }) { exam ->
                    val isSelected = exam.id == selectedExamId
                    ExamCard(
                        exam = exam,
                        isSelected = isSelected,
                        onSelect = { selectedExamId = exam.id }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    // Paper Level Selector Card
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0x18FFFFFF),
                        border = BorderStroke(1.dp, SaffronYellow.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "तैयारी का स्तर (Paper Level):",
                                    color = PaperLight,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = currentExam.code,
                                    color = SaffronYellow,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            currentExam.availablePapers.forEach { paper ->
                                val isPaperSelected = selectedPaper == paper
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isPaperSelected) Color(0x33F6A91B) else Color(0x12FFFFFF))
                                        .border(
                                            1.dp,
                                            if (isPaperSelected) SaffronYellow else Color.Transparent,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable { selectedPaper = paper }
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = paper.label,
                                            color = if (isPaperSelected) PaperLight else Color(0xFFCFE0D7),
                                            fontSize = 13.sp,
                                            fontWeight = if (isPaperSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                        Text(
                                            text = paper.subtitle,
                                            color = Color(0xFF8FC3B4),
                                            fontSize = 11.sp
                                        )
                                    }

                                    if (isPaperSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(22.dp)
                                                .clip(CircleShape)
                                                .background(SaffronYellow),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = DeepGreenDark,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(84.dp))
                }
            }
        }

        // ================= BOTTOM FIXED ACTION BAR =================
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = Color(0xEB082720),
            border = BorderStroke(1.dp, Color(0x28FFFFFF))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "चयनित: ${currentExam.code}",
                        color = SaffronYellow,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${selectedPaper.label} • 150 अंक",
                        color = Color(0xFF8FC3B4),
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Button(
                    onClick = { onExamSelected(currentExam, selectedPaper) },
                    modifier = Modifier
                        .height(48.dp)
                        .widthIn(min = 160.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SaffronYellow,
                        contentColor = DeepGreenDark
                    ),
                    shape = RoundedCornerShape(99.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                ) {
                    Text(
                        text = "आगे बढ़ें",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Continue",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ExamCard(
    exam: Exam,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) Color(0x28FFFFFF) else Color(0x14FFFFFF),
        border = BorderStroke(
            if (isSelected) 1.8.dp else 1.dp,
            if (isSelected) SaffronYellow else Color(0x26FFFFFF)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Exam Code Badge Box
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) SaffronYellow else DeepGreenSurface)
                        .border(
                            1.dp,
                            if (isSelected) SaffronDark else Color(0x33FFFFFF),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = exam.code.take(4),
                        color = if (isSelected) DeepGreenDark else PaperLight,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exam.titleHindi,
                        color = PaperLight,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = exam.stateAuthority,
                        color = Color(0xFF8FC3B4),
                        fontSize = 11.sp,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(
                            shape = RoundedCornerShape(99.dp),
                            color = if (exam.isHot) VermilionRed.copy(alpha = 0.2f) else Color(0x20FFFFFF),
                            border = BorderStroke(
                                0.8.dp,
                                if (exam.isHot) VermilionRed else Color(0x40FFFFFF)
                            )
                        ) {
                            Text(
                                text = exam.badgeText,
                                color = if (exam.isHot) Color(0xFFFF8B80) else Color(0xFFB5C9C0),
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }

                        Text(
                            text = "${exam.totalSubjects} विषय • 150 प्रश्न",
                            color = Color(0xFF7E978F),
                            fontSize = 10.sp,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }
                }
            }

            // Radio/Selection Circle
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) SaffronYellow else Color.Transparent)
                    .border(
                        1.5.dp,
                        if (isSelected) SaffronYellow else Color(0x66FFFFFF),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = DeepGreenDark,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}
