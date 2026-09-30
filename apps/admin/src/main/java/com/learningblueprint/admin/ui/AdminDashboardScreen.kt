package com.learningblueprint.admin.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.learningblueprint.admin.sync.AdminRemoteSyncManager
import com.learningblueprint.core.model.*
import com.learningblueprint.core.theme.*
import kotlinx.coroutines.launch

@Composable
fun AdminDashboardScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) }

    // 1. INITIAL CACHE LOAD (Prevent reverting to defaults on restart)
    val initConfig = remember {
        val prefs = context.getSharedPreferences("admin_sync_store", Context.MODE_PRIVATE)
        val json = prefs.getString("published_config", null)
        if (json != null) AppConfig.fromJsonString(json) ?: AppConfig.DEFAULT else AppConfig.DEFAULT
    }

    // 2. GENERATE DEFAULT MASTER LISTS IF EMPTY
    val defaultAllSubjects = remember { Exam.ALL_EXAMS.flatMap { e -> ExamPaper.values().flatMap { p -> Subject.getSubjectsForExam(e.id, p) } } }
    val defaultAllChapters = remember { defaultAllSubjects.map { it.code }.distinct().flatMap { c -> Chapter.getChaptersForSubject(c) }.distinctBy { it.id } }
    val defaultAllQuestions = remember { defaultAllChapters.flatMap { Question.getQuestionsForChapter(it.id) }.distinctBy { it.id } }

    var announcements by remember { mutableStateOf(initConfig.announcements) }
    var socialLinks by remember { mutableStateOf(initConfig.socialLinks) }
    var exams by remember { mutableStateOf(initConfig.exams) }

    // 3. MASTER STATES (Contains all data across all exams)
    var allSubjects by remember { mutableStateOf(if (initConfig.subjects.isNotEmpty()) initConfig.subjects else defaultAllSubjects) }
    var allChapters by remember { mutableStateOf(if (initConfig.chapters.isNotEmpty()) initConfig.chapters else defaultAllChapters) }
    var allQuestions by remember { mutableStateOf(if (initConfig.questions.isNotEmpty()) initConfig.questions else defaultAllQuestions) }

    // 4. DERIVED UI STATES (Filtered specifically for hierarchical drill-down display)
    var selectedCurriculumExam by remember { mutableStateOf(exams.firstOrNull() ?: Exam.ALL_EXAMS.first()) }
    var selectedCurriculumPaper by remember { mutableStateOf(ExamPaper.PAPER_1) }

    val subjects = allSubjects.filter { it.examId == selectedCurriculumExam.id && it.paper == selectedCurriculumPaper }
    var selectedSubjectForChapters by remember { mutableStateOf<Subject?>(null) }
    val chapters = if (selectedSubjectForChapters != null) allChapters.filter { it.subjectCode == selectedSubjectForChapters!!.code } else emptyList()

    var selectedChapterForQuestions by remember { mutableStateOf<Chapter?>(null) }
    val questions = if (selectedChapterForQuestions != null) allQuestions.filter { it.chapterId == selectedChapterForQuestions!!.id } else emptyList()

    var isPublishing by remember { mutableStateOf(false) }

    // 5. DIALOG STATES
    var showAddNoticeDialog by remember { mutableStateOf(false) }
    var editingSocialLink by remember { mutableStateOf<SocialLink?>(null) }
    var editingExam by remember { mutableStateOf<Exam?>(null) }
    var editingSubject by remember { mutableStateOf<Subject?>(null) }
    var editingChapter by remember { mutableStateOf<Chapter?>(null) }
    var editingQuestion by remember { mutableStateOf<Question?>(null) }
    var showAddQuestionDialog by remember { mutableStateOf(false) }
    var showBulkPasteDialog by remember { mutableStateOf(false) }
    var showCloudSettingsDialog by remember { mutableStateOf(false) }
    var gitHubConfig by remember { mutableStateOf(AdminRemoteSyncManager.getGitHubConfig(context)) }

    fun triggerPublish() {
        coroutineScope.launch {
            isPublishing = true
            val liveNotices = announcements.filter { it.isPublished }
            val ticker = liveNotices.firstOrNull() ?: AppConfig.DEFAULT.tickerAnnouncement

            // BUNDLE FULL MASTER LISTS (Screens 1 to 5)
            val config = AppConfig(
                tickerAnnouncement = ticker,
                announcements = announcements,
                socialLinks = socialLinks,
                exams = exams,
                subjects = allSubjects,
                chapters = allChapters,
                questions = allQuestions,
                lastSyncTime = System.currentTimeMillis()
            )

            val (_, message) = AdminRemoteSyncManager.publish(context, config)
            isPublishing = false
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF06221C), DeepGreenDark)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // ================= HEADER ROW =================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "गुरु जी • GURU JI",
                        color = SaffronYellow,
                        fontFamily = RozhaOneFontFamily,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "कंटेंट एवं पाठ्यक्रम कंट्रोल सेंटर",
                        color = Color(0xFF8FC3B4),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = { showCloudSettingsDialog = true },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0x22FFFFFF))
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = SaffronYellow, modifier = Modifier.size(18.dp))
                    }

                    Button(
                        onClick = { if (!isPublishing) triggerPublish() },
                        colors = ButtonDefaults.buttonColors(containerColor = SaffronYellow, contentColor = DeepGreenDark),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Publish", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = if (isPublishing) "पब्लिशिंग..." else "पब्लिश", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ================= 5-TAB NAVIGATION BAR =================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x18FFFFFF))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("🔔 सूचना", "🌐 लिंक्स", "📚 परीक्षा", "📖 पाठ्यक्रम", "📱 सिंक").forEachIndexed { index, title ->
                    val isSelected = selectedTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (isSelected) SaffronYellow else Color.Transparent)
                            .clickable { selectedTab = index }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            color = if (isSelected) DeepGreenDark else PaperLight,
                            fontSize = 10.5.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            when (selectedTab) {
                0 -> {
                    // TAB 0: NOTICES (SCREEN 1)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "कुल सूचनाएं (${announcements.size})", color = PaperLight, fontSize = 15.sp, fontWeight = FontWeight.Bold)

                        Button(
                            onClick = { showAddNoticeDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x28FFFFFF), contentColor = PaperLight),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "नया नोटिस", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(announcements, key = { it.id }) { notice ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (notice.isPublished) Color(0x22FFFFFF) else Color(0x10FFFFFF),
                                border = BorderStroke(1.dp, if (notice.isPublished) SaffronYellow.copy(alpha = 0.4f) else Color(0x22FFFFFF)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (notice.isPublished) "✓ लाइव (Live)" else "✗ अप्रकाशित (Draft)",
                                            color = if (notice.isPublished) Color(0xFF1C9E5F) else Color(0xFFDD4F3A),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Switch(
                                                checked = notice.isPublished,
                                                onCheckedChange = {
                                                    announcements = announcements.map {
                                                        if (it.id == notice.id) it.copy(isPublished = !it.isPublished) else it
                                                    }
                                                },
                                                colors = SwitchDefaults.colors(checkedThumbColor = SaffronYellow)
                                            )
                                            IconButton(
                                                onClick = { announcements = announcements.filter { it.id != notice.id } },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Default.Close, contentDescription = "Delete", tint = Color(0xFFFF6B60), modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }

                                    Text(text = notice.title, color = PaperLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(text = notice.message, color = Color(0xFFB9C9C0), fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 1: SOCIAL LINKS (SCREEN 1)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "सोशल मीडिया चैनल प्रबंधन", color = PaperLight, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text(text = "लिंक बदलने हेतु 'बदलें' बटन दबाएँ", color = Color(0xFF8FC3B4), fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(socialLinks, key = { it.platform }) { link ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0x18FFFFFF),
                                border = BorderStroke(1.dp, Color(0x28FFFFFF)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        SocialBrandIcon(platform = link.platform, sizeDp = 42.dp)

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = link.label, color = PaperLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                            Text(text = link.url, color = Color(0xFF8FC3B4), fontSize = 11.sp, maxLines = 1)
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Button(
                                            onClick = { editingSocialLink = link },
                                            colors = ButtonDefaults.buttonColors(containerColor = SaffronYellow, contentColor = DeepGreenDark),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text("बदलें", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Switch(
                                            checked = link.isEnabled,
                                            onCheckedChange = {
                                                socialLinks = socialLinks.map {
                                                    if (it.platform == link.platform) it.copy(isEnabled = !it.isEnabled) else it
                                                }
                                            },
                                            colors = SwitchDefaults.colors(checkedThumbColor = SaffronYellow)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // TAB 2: EXAM MANAGEMENT (SCREEN 2)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "शिक्षक पात्रता परीक्षा प्रबंधन (${exams.size})", color = PaperLight, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text(text = "छात्र ऐप में परीक्षा लाइव करने या छिपाने हेतु स्विच बदलें", color = Color(0xFF8FC3B4), fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(exams, key = { it.id }) { exam ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (exam.isPublished) Color(0x22FFFFFF) else Color(0x10FFFFFF),
                                border = BorderStroke(1.2.dp, if (exam.isPublished) SaffronYellow.copy(alpha = 0.5f) else Color(0x22FFFFFF)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Surface(shape = RoundedCornerShape(6.dp), color = if (exam.isPublished) SaffronYellow else Color(0x33FFFFFF)) {
                                                Text(
                                                    text = exam.code,
                                                    color = if (exam.isPublished) DeepGreenDark else PaperLight,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }

                                            Text(
                                                text = if (exam.isPublished) "✓ लाइव (Active)" else "✗ बंद (Hidden)",
                                                color = if (exam.isPublished) Color(0xFF1C9E5F) else Color(0xFFDD4F3A),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Button(
                                                onClick = { editingExam = exam },
                                                colors = ButtonDefaults.buttonColors(containerColor = SaffronYellow, contentColor = DeepGreenDark),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                modifier = Modifier.height(30.dp)
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text("संपादित", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                            }

                                            Switch(
                                                checked = exam.isPublished,
                                                onCheckedChange = { isChecked ->
                                                    exams = exams.map {
                                                        if (it.id == exam.id) it.copy(isPublished = isChecked) else it
                                                    }
                                                },
                                                colors = SwitchDefaults.colors(checkedThumbColor = SaffronYellow)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(text = exam.titleHindi, color = PaperLight, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(text = exam.stateAuthority, color = Color(0xFF8FC3B4), fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(99.dp),
                                            color = if (exam.isHot) VermilionRed.copy(alpha = 0.25f) else Color(0x20FFFFFF),
                                            border = BorderStroke(0.8.dp, if (exam.isHot) VermilionRed else Color(0x33FFFFFF))
                                        ) {
                                            Text(
                                                text = exam.badgeText,
                                                color = if (exam.isHot) Color(0xFFFF8B80) else Color(0xFFB5C9C0),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }

                                        Text(text = "हॉट बैज:", color = Color(0xFF8FC3B4), fontSize = 10.5.sp)

                                        Switch(
                                            checked = exam.isHot,
                                            onCheckedChange = { isHotChecked ->
                                                exams = exams.map {
                                                    if (it.id == exam.id) it.copy(isHot = isHotChecked) else it
                                                }
                                            },
                                            colors = SwitchDefaults.colors(checkedThumbColor = VermilionRed),
                                            modifier = Modifier.height(24.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // TAB 3: CURRICULUM MANAGEMENT (SCREEN 3 SUBJECTS, SCREEN 4 CHAPTERS, SCREEN 5 QUESTIONS)
                    val activeSubject = selectedSubjectForChapters

                    if (activeSubject == null) {
                        // ================= LEVEL 1: SCREEN 3 SUBJECT MANAGEMENT =================
                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = "विषय ब्लूप्रिंट प्रबंधन (Screen 3)", color = PaperLight, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                    Text(text = "परीक्षा व पेपर चुनें ➔ विषय लाइव करें या संपादित करें", color = Color(0xFF8FC3B4), fontSize = 11.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Exam and Paper Level Selectors
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0x20FFFFFF),
                                    border = BorderStroke(1.dp, SaffronYellow.copy(alpha = 0.5f)),
                                    modifier = Modifier.weight(1f).clickable {
                                        val currentIndex = exams.indexOfFirst { it.id == selectedCurriculumExam.id }
                                        val nextIndex = (currentIndex + 1) % exams.size
                                        selectedCurriculumExam = exams[nextIndex]
                                    }
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text("लक्ष्य परीक्षा", color = Color(0xFF8FC3B4), fontSize = 9.sp)
                                        Text(selectedCurriculumExam.code, color = SaffronYellow, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0x20FFFFFF),
                                    border = BorderStroke(1.dp, Color(0x33FFFFFF)),
                                    modifier = Modifier.weight(1f).clickable {
                                        selectedCurriculumPaper = when (selectedCurriculumPaper) {
                                            ExamPaper.PAPER_1 -> ExamPaper.PAPER_2
                                            ExamPaper.PAPER_2 -> ExamPaper.BOTH
                                            ExamPaper.BOTH -> ExamPaper.PAPER_1
                                        }
                                    }
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text("तैयारी स्तर", color = Color(0xFF8FC3B4), fontSize = 9.sp)
                                        Text(selectedCurriculumPaper.label.take(15), color = PaperLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(subjects, key = { it.id }) { subject ->
                                    val accentColor = Color(subject.hexColor)

                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (subject.isPublished) Color(0x20FFFFFF) else Color(0x10FFFFFF),
                                        border = BorderStroke(1.dp, if (subject.isPublished) accentColor.copy(alpha = 0.5f) else Color(0x22FFFFFF)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    Surface(shape = RoundedCornerShape(6.dp), color = accentColor.copy(alpha = 0.2f), border = BorderStroke(0.8.dp, accentColor)) {
                                                        Text(
                                                            text = subject.code,
                                                            color = accentColor,
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                    Text(
                                                        text = if (subject.isPublished) "✓ लाइव" else "✗ ड्राफ्ट",
                                                        color = if (subject.isPublished) Color(0xFF1C9E5F) else Color(0xFFDD4F3A),
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }

                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    Button(
                                                        onClick = { editingSubject = subject },
                                                        colors = ButtonDefaults.buttonColors(containerColor = SaffronYellow, contentColor = DeepGreenDark),
                                                        shape = RoundedCornerShape(8.dp),
                                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                        modifier = Modifier.height(28.dp)
                                                    ) {
                                                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(11.dp))
                                                        Spacer(modifier = Modifier.width(3.dp))
                                                        Text("संपादित", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                    }

                                                    Switch(
                                                        checked = subject.isPublished,
                                                        onCheckedChange = { isChecked ->
                                                            allSubjects = allSubjects.map {
                                                                if (it.id == subject.id) it.copy(isPublished = isChecked) else it
                                                            }
                                                        },
                                                        colors = SwitchDefaults.colors(checkedThumbColor = SaffronYellow),
                                                        modifier = Modifier.height(24.dp)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(text = subject.titleHindi, color = PaperLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                            Text(text = subject.titleEnglish, color = Color(0xFF8FC3B4), fontSize = 10.5.sp)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(text = subject.descriptionHindi, color = Color(0xFFCFE0D7), fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)

                                            Spacer(modifier = Modifier.height(8.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(text = "${subject.marks} अंक • ${subject.chaptersCount} अध्याय", color = SaffronYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                                                Button(
                                                    onClick = { selectedSubjectForChapters = subject },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x28FFFFFF), contentColor = PaperLight),
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                                    modifier = Modifier.height(28.dp)
                                                ) {
                                                    Text("अध्याय देखें", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "View", modifier = Modifier.size(12.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else if (selectedChapterForQuestions == null) {
                        // ================= LEVEL 2: SCREEN 4 CHAPTER ROADMAP MANAGEMENT =================
                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { selectedSubjectForChapters = null },
                                    modifier = Modifier.size(34.dp).clip(CircleShape).background(Color(0x22FFFFFF))
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PaperLight, modifier = Modifier.size(16.dp))
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(
                                        text = "${activeSubject.code} • अध्याय प्रबंधन (Screen 4)",
                                        color = PaperLight,
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = activeSubject.titleHindi,
                                        color = SaffronYellow,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(chapters, key = { it.id }) { chapter ->
                                    val chapterQuestionCount = allQuestions.count { it.chapterId == chapter.id }

                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (chapter.isPublished) Color(0x20FFFFFF) else Color(0x10FFFFFF),
                                        border = BorderStroke(
                                            1.dp,
                                            if (chapter.isHighYield) SaffronYellow.copy(alpha = 0.6f) else Color(0x24FFFFFF)
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = if (chapter.isHighYield) SaffronYellow else Color(0x28FFFFFF)
                                                    ) {
                                                        Text(
                                                            text = "CH ${chapter.chapterNumber}",
                                                            color = if (chapter.isHighYield) DeepGreenDark else PaperLight,
                                                            fontSize = 10.5.sp,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }

                                                    Text(
                                                        text = if (chapter.isPublished) "✓ लाइव" else "✗ ड्राफ्ट",
                                                        color = if (chapter.isPublished) Color(0xFF1C9E5F) else Color(0xFFDD4F3A),
                                                        fontSize = 10.5.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }

                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    Button(
                                                        onClick = { editingChapter = chapter },
                                                        colors = ButtonDefaults.buttonColors(containerColor = SaffronYellow, contentColor = DeepGreenDark),
                                                        shape = RoundedCornerShape(8.dp),
                                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                        modifier = Modifier.height(28.dp)
                                                    ) {
                                                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(11.dp))
                                                        Spacer(modifier = Modifier.width(3.dp))
                                                        Text("संपादित", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                    }

                                                    Switch(
                                                        checked = chapter.isPublished,
                                                        onCheckedChange = { isChecked ->
                                                            allChapters = allChapters.map {
                                                                if (it.id == chapter.id) it.copy(isPublished = isChecked) else it
                                                            }
                                                        },
                                                        colors = SwitchDefaults.colors(checkedThumbColor = SaffronYellow),
                                                        modifier = Modifier.height(24.dp)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(text = chapter.titleHindi, color = PaperLight, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                                            Text(text = chapter.titleEnglish, color = Color(0xFF8FC3B4), fontSize = 10.5.sp)

                                            Spacer(modifier = Modifier.height(6.dp))

                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Surface(
                                                    shape = RoundedCornerShape(99.dp),
                                                    color = if (chapter.isHighYield) VermilionRed.copy(alpha = 0.2f) else Color(0x18FFFFFF),
                                                    border = BorderStroke(0.8.dp, if (chapter.isHighYield) VermilionRed else Color(0x33FFFFFF))
                                                ) {
                                                    Text(
                                                        text = chapter.importanceBadge,
                                                        color = if (chapter.isHighYield) Color(0xFFFF8B80) else Color(0xFFB5C9C0),
                                                        fontSize = 9.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                                    )
                                                }

                                                Text(text = "अति-महत्वपूर्ण:", color = Color(0xFF8FC3B4), fontSize = 10.sp)

                                                Switch(
                                                    checked = chapter.isHighYield,
                                                    onCheckedChange = { isHighYieldChecked ->
                                                        allChapters = allChapters.map {
                                                            if (it.id == chapter.id) it.copy(isHighYield = isHighYieldChecked) else it
                                                        }
                                                    },
                                                    colors = SwitchDefaults.colors(checkedThumbColor = VermilionRed),
                                                    modifier = Modifier.height(22.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))

                                            // CTA to Screen 5: Question Bank Management
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "PYQ बैंक: $chapterQuestionCount प्रश्न",
                                                    color = SaffronYellow,
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.Bold
                                                )

                                                Button(
                                                    onClick = { selectedChapterForQuestions = chapter },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x28FFFFFF), contentColor = PaperLight),
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                                    modifier = Modifier.height(28.dp)
                                                ) {
                                                    Text("प्रश्न बैंक देखें", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "View", modifier = Modifier.size(12.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // ================= LEVEL 3: SCREEN 5 QUESTION BANK MANAGEMENT =================
                        val activeChapter = selectedChapterForQuestions!!

                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { selectedChapterForQuestions = null },
                                        modifier = Modifier.size(34.dp).clip(CircleShape).background(Color(0x22FFFFFF))
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PaperLight, modifier = Modifier.size(16.dp))
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column {
                                        Text(
                                            text = "CH ${activeChapter.chapterNumber} • प्रश्न बैंक (${questions.size})",
                                            color = PaperLight,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = activeChapter.titleHindi,
                                            color = SaffronYellow,
                                            fontSize = 10.5.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Button(
                                        onClick = { showBulkPasteDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C9E5F), contentColor = PaperLight),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text("📋 बल्क पेस्ट", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = { showAddQuestionDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = SaffronYellow, contentColor = DeepGreenDark),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text("प्रश्न जोड़ें", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            if (questions.isEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0x12FFFFFF),
                                    modifier = Modifier.fillMaxWidth().padding(top = 20.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("इस अध्याय में अभी कोई प्रश्न नहीं हैं", color = PaperLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("ऊपर 'बल्क पेस्ट' बटन दबाकर 30 से 150 प्रश्न एक साथ जोड़ें।", color = Color(0xFF8FC3B4), fontSize = 11.5.sp)
                                    }
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    items(questions.withIndex().toList(), key = { it.value.id }) { (index, question) ->
                                        Surface(
                                            shape = RoundedCornerShape(14.dp),
                                            color = Color(0x15FFFFFF),
                                            border = BorderStroke(1.dp, Color(0x28FFFFFF)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = SaffronYellow.copy(alpha = 0.2f),
                                                        border = BorderStroke(0.8.dp, SaffronYellow)
                                                    ) {
                                                        Text(
                                                            text = "Q${index + 1} • ${question.examYearText}",
                                                            color = SaffronYellow,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }

                                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                        IconButton(
                                                            onClick = { editingQuestion = question },
                                                            modifier = Modifier.size(26.dp)
                                                        ) {
                                                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = PaperLight, modifier = Modifier.size(13.dp))
                                                        }
                                                        IconButton(
                                                            onClick = {
                                                                allQuestions = allQuestions.filter { it.id != question.id }
                                                                val newCount = allQuestions.count { it.chapterId == activeChapter.id }
                                                                allChapters = allChapters.map { if (it.id == activeChapter.id) it.copy(pyqCount = newCount) else it }
                                                            },
                                                            modifier = Modifier.size(26.dp)
                                                        ) {
                                                            Icon(Icons.Default.Close, contentDescription = "Delete", tint = Color(0xFFFF6B60), modifier = Modifier.size(14.dp))
                                                        }
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = question.questionHindi,
                                                    color = PaperLight,
                                                    fontSize = 12.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )

                                                Spacer(modifier = Modifier.height(4.dp))
                                                val correctOptText = question.options.getOrNull(question.correctOptionIndex) ?: ""
                                                Text(
                                                    text = "✓ सही उत्तर (${question.correctOptionIndex + 1}): $correctOptText",
                                                    color = Color(0xFF1C9E5F),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )

                                                if (question.explanationHindi.isNotBlank()) {
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = "व्याख्या: ${question.explanationHindi}",
                                                        color = Color(0xFFCFE0D7),
                                                        fontSize = 10.5.sp,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                4 -> {
                    // TAB 4: REMOTE SYNC STATUS
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0x20FFFFFF),
                        border = BorderStroke(1.2.dp, SaffronYellow.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "🌐 क्रॉस-डिवाइस रिमोट सिंक स्थिति", color = SaffronYellow, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "अब आप किसी भी दूसरे फोन पर मौजूद स्टूडेंट ऐप में सीधे 5-स्क्रीन पाठ्यक्रम, परीक्षा, नोटिस, अध्याय व प्रश्नोत्तरी भेज सकते हैं।",
                                color = Color(0xFFCFE0D7),
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(text = "• GitHub रिपॉजिटरी: ${gitHubConfig.owner}/${gitHubConfig.repo} (${gitHubConfig.branch})", color = Color(0xFF1C9E5F), fontSize = 12.5.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "• पब्लिक रॉ URL: ${gitHubConfig.rawUrl}", color = Color(0xFF8FC3B4), fontSize = 10.5.sp)
                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = { showCloudSettingsDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = SaffronYellow, contentColor = DeepGreenDark),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("क्लाउड / GitHub सेटिंग्स बदलें", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // ================= BULK QUESTION IMPORT DIALOG (30–150 QUESTIONS PASTE) =================
    if (showBulkPasteDialog && selectedChapterForQuestions != null) {
        val targetCh = selectedChapterForQuestions!!
        var pastedText by remember { mutableStateOf("") }
        var parseResult by remember { mutableStateOf<BatchQuestionParser.ParseResult?>(null) }
        var isParsing by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = { showBulkPasteDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF07261F)),
                border = BorderStroke(1.2.dp, SaffronYellow.copy(alpha = 0.7f)),
                modifier = Modifier.fillMaxWidth().fillMaxHeight(0.92f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "📋 बल्क प्रश्न आयात (30-150 PYQs)", color = PaperLight, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text(text = "CH ${targetCh.chapterNumber}: ${targetCh.titleHindi}", color = SaffronYellow, fontSize = 10.5.sp, maxLines = 1)
                        }
                        IconButton(onClick = { showBulkPasteDialog = false }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = PaperLight)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = pastedText,
                        onValueChange = {
                            pastedText = it
                            parseResult = null
                        },
                        label = { Text("प्रश्न-पत्र का सम्पूर्ण टेक्स्ट यहाँ पेस्ट करें (अभिकथन, 4 विकल्प, उत्तर व सम्पूर्ण व्याख्या)") },
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        minLines = 8,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SaffronYellow,
                            unfocusedBorderColor = Color(0x33FFFFFF)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Parse / Audit Summary View
                    parseResult?.let { result ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (result.failedCount == 0) Color(0x221C9E5F) else Color(0x22DD4F3A),
                            border = BorderStroke(1.dp, if (result.failedCount == 0) Color(0xFF1C9E5F) else Color(0xFFDD4F3A)),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = "जाँच परिणाम: ${result.successfulQuestions.size} प्रश्न सफलतापूर्वक पहचाने गए (कुल खोजे: ${result.totalDetected})",
                                    color = PaperLight,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (result.errors.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "त्रुटियाँ: ${result.errors.take(2).joinToString(" | ")}",
                                        color = Color(0xFFFF8B80),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                isParsing = true
                                parseResult = BatchQuestionParser.parseRawText(
                                    rawText = pastedText,
                                    chapterId = targetCh.id,
                                    subjectCode = selectedSubjectForChapters?.code ?: targetCh.subjectCode,
                                    defaultExamYear = "CTET Official PYQ"
                                )
                                isParsing = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FFFFFF), contentColor = PaperLight),
                            modifier = Modifier.weight(1f),
                            enabled = pastedText.isNotBlank() && !isParsing
                        ) {
                            Text(if (isParsing) "पार्सिंग..." else "1. जाँचें व पार्स करें", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val toAdd = parseResult?.successfulQuestions ?: emptyList()
                                if (toAdd.isNotEmpty()) {
                                    allQuestions = allQuestions + toAdd
                                    val newCount = allQuestions.count { it.chapterId == targetCh.id }
                                    allChapters = allChapters.map { if (it.id == targetCh.id) it.copy(pyqCount = newCount) else it }
                                    showBulkPasteDialog = false
                                    Toast.makeText(context, "${toAdd.size} प्रश्न सफलतापूर्वक जोड़े गए! पब्लिश करें दबाएँ।", Toast.LENGTH_LONG).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronYellow, contentColor = DeepGreenDark),
                            modifier = Modifier.weight(1f),
                            enabled = parseResult != null && parseResult!!.successfulQuestions.isNotEmpty()
                        ) {
                            Text("2. बैंक में जोड़ें", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // ================= SINGLE QUESTION ADD / EDIT DIALOG =================
    val targetQuestion = editingQuestion ?: if (showAddQuestionDialog && selectedChapterForQuestions != null) {
        Question(
            id = "q_${selectedChapterForQuestions!!.id}_${System.currentTimeMillis()}",
            chapterId = selectedChapterForQuestions!!.id,
            subjectCode = selectedSubjectForChapters?.code ?: selectedChapterForQuestions!!.subjectCode,
            examYearText = "CTET PYQ",
            questionHindi = "",
            questionEnglish = "",
            options = listOf("", "", "", ""),
            correctOptionIndex = 0,
            explanationHindi = ""
        )
    } else null

    if (targetQuestion != null) {
        var editExamYear by remember { mutableStateOf(targetQuestion.examYearText) }
        var editQHindi by remember { mutableStateOf(targetQuestion.questionHindi) }
        var optA by remember { mutableStateOf(targetQuestion.options.getOrNull(0) ?: "") }
        var optB by remember { mutableStateOf(targetQuestion.options.getOrNull(1) ?: "") }
        var optC by remember { mutableStateOf(targetQuestion.options.getOrNull(2) ?: "") }
        var optD by remember { mutableStateOf(targetQuestion.options.getOrNull(3) ?: "") }
        var correctIdx by remember { mutableIntStateOf(targetQuestion.correctOptionIndex) }
        var editExplanation by remember { mutableStateOf(targetQuestion.explanationHindi) }

        Dialog(onDismissRequest = { editingQuestion = null; showAddQuestionDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF07261F)),
                border = BorderStroke(1.2.dp, SaffronYellow.copy(alpha = 0.7f)),
                modifier = Modifier.fillMaxWidth().fillMaxHeight(0.92f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (editingQuestion != null) "प्रश्न संपादित करें" else "नया प्रश्न जोड़ें",
                        color = PaperLight,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            OutlinedTextField(
                                value = editExamYear,
                                onValueChange = { editExamYear = it },
                                label = { Text("परीक्षा टैग (e.g. CTET 2024 / UPTET)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = editQHindi,
                                onValueChange = { editQHindi = it },
                                label = { Text("प्रश्न (हिंदी)") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2
                            )
                        }
                        item { OutlinedTextField(value = optA, onValueChange = { optA = it }, label = { Text("विकल्प 1 (A)") }, modifier = Modifier.fillMaxWidth()) }
                        item { OutlinedTextField(value = optB, onValueChange = { optB = it }, label = { Text("विकल्प 2 (B)") }, modifier = Modifier.fillMaxWidth()) }
                        item { OutlinedTextField(value = optC, onValueChange = { optC = it }, label = { Text("विकल्प 3 (C)") }, modifier = Modifier.fillMaxWidth()) }
                        item { OutlinedTextField(value = optD, onValueChange = { optD = it }, label = { Text("विकल्प 4 (D)") }, modifier = Modifier.fillMaxWidth()) }

                        item {
                            Text("सही उत्तर विकल्प चुनें:", color = Color(0xFF8FC3B4), fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("1", "2", "3", "4").forEachIndexed { idx, label ->
                                    Button(
                                        onClick = { correctIdx = idx },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (correctIdx == idx) Color(0xFF1C9E5F) else Color(0x22FFFFFF),
                                            contentColor = PaperLight
                                        ),
                                        modifier = Modifier.weight(1f).height(32.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text(text = "($label)", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }
                            }
                        }

                        item {
                            OutlinedTextField(
                                value = editExplanation,
                                onValueChange = { editExplanation = it },
                                label = { Text("विस्तृत व्याख्या व पेडागॉजिकल विश्लेषण") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { editingQuestion = null; showAddQuestionDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("रद्द करें", color = PaperLight)
                        }

                        Button(
                            onClick = {
                                val updated = targetQuestion.copy(
                                    examYearText = editExamYear.trim(),
                                    questionHindi = editQHindi.trim(),
                                    options = listOf(optA.trim(), optB.trim(), optC.trim(), optD.trim()),
                                    correctOptionIndex = correctIdx,
                                    explanationHindi = editExplanation.trim()
                                )
                                allQuestions = if (editingQuestion != null) {
                                    allQuestions.map { if (it.id == updated.id) updated else it }
                                } else {
                                    listOf(updated) + allQuestions
                                }
                                val activeCh = selectedChapterForQuestions
                                if (activeCh != null) {
                                    val newCount = allQuestions.count { it.chapterId == activeCh.id }
                                    allChapters = allChapters.map { if (it.id == activeCh.id) it.copy(pyqCount = newCount) else it }
                                }
                                editingQuestion = null
                                showAddQuestionDialog = false
                                Toast.makeText(context, "प्रश्न सुरक्षित हुआ!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronYellow, contentColor = DeepGreenDark),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("सुरक्षित करें", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // ================= SUBJECT EDIT DIALOG (SCREEN 3) =================
    editingSubject?.let { currentSubject ->
        var editTitleHindi by remember { mutableStateOf(currentSubject.titleHindi) }
        var editTitleEnglish by remember { mutableStateOf(currentSubject.titleEnglish) }
        var editDescription by remember { mutableStateOf(currentSubject.descriptionHindi) }
        var editMarks by remember { mutableStateOf(currentSubject.marks.toString()) }

        Dialog(onDismissRequest = { editingSubject = null }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A2E26)),
                border = BorderStroke(1.2.dp, SaffronYellow.copy(alpha = 0.7f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(text = "${currentSubject.code} विषय विवरण संपादित करें", color = PaperLight, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(value = editTitleHindi, onValueChange = { editTitleHindi = it }, label = { Text("विषय का नाम (हिंदी)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = editTitleEnglish, onValueChange = { editTitleEnglish = it }, label = { Text("Subject Name (English)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = editDescription, onValueChange = { editDescription = it }, label = { Text("सिलेबस विवरण") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = editMarks, onValueChange = { editMarks = it }, label = { Text("कुल अंक वेटेज") }, modifier = Modifier.fillMaxWidth())

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { editingSubject = null }, modifier = Modifier.weight(1f)) {
                            Text("रद्द करें", color = PaperLight)
                        }

                        Button(
                            onClick = {
                                val parsedMarks = editMarks.toIntOrNull() ?: currentSubject.marks
                                allSubjects = allSubjects.map {
                                    if (it.id == currentSubject.id) it.copy(
                                        titleHindi = editTitleHindi.trim(),
                                        titleEnglish = editTitleEnglish.trim(),
                                        descriptionHindi = editDescription.trim(),
                                        marks = parsedMarks
                                    ) else it
                                }
                                editingSubject = null
                                Toast.makeText(context, "${currentSubject.code} अपडेट हुआ! पब्लिश करें दबाएँ।", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronYellow, contentColor = DeepGreenDark),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("सुरक्षित करें", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // ================= CHAPTER EDIT DIALOG (SCREEN 4) =================
    editingChapter?.let { currentChapter ->
        var editTitleHindi by remember { mutableStateOf(currentChapter.titleHindi) }
        var editTitleEnglish by remember { mutableStateOf(currentChapter.titleEnglish) }
        var editBadgeText by remember { mutableStateOf(currentChapter.importanceBadge) }
        var editPyqCount by remember { mutableStateOf(currentChapter.pyqCount.toString()) }

        Dialog(onDismissRequest = { editingChapter = null }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A2E26)),
                border = BorderStroke(1.2.dp, SaffronYellow.copy(alpha = 0.7f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(text = "CH ${currentChapter.chapterNumber} विवरण संपादित करें", color = PaperLight, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(value = editTitleHindi, onValueChange = { editTitleHindi = it }, label = { Text("अध्याय का नाम (हिंदी)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = editTitleEnglish, onValueChange = { editTitleEnglish = it }, label = { Text("Chapter Name (English)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = editBadgeText, onValueChange = { editBadgeText = it }, label = { Text("महत्व बैज (e.g. ★ 4-5 प्रश्न)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = editPyqCount, onValueChange = { editPyqCount = it }, label = { Text("PYQ प्रश्नों की संख्या") }, modifier = Modifier.fillMaxWidth())

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { editingChapter = null }, modifier = Modifier.weight(1f)) {
                            Text("रद्द करें", color = PaperLight)
                        }

                        Button(
                            onClick = {
                                val parsedPyq = editPyqCount.toIntOrNull() ?: currentChapter.pyqCount
                                allChapters = allChapters.map {
                                    if (it.id == currentChapter.id) it.copy(
                                        titleHindi = editTitleHindi.trim(),
                                        titleEnglish = editTitleEnglish.trim(),
                                        importanceBadge = editBadgeText.trim(),
                                        pyqCount = parsedPyq
                                    ) else it
                                }
                                editingChapter = null
                                Toast.makeText(context, "अध्याय ${currentChapter.chapterNumber} अपडेट हुआ!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronYellow, contentColor = DeepGreenDark),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("सुरक्षित करें", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // ================= EXAM EDIT DIALOG =================
    editingExam?.let { currentExam ->
        var editTitleHindi by remember { mutableStateOf(currentExam.titleHindi) }
        var editBadgeText by remember { mutableStateOf(currentExam.badgeText) }
        var editAuthority by remember { mutableStateOf(currentExam.stateAuthority) }

        Dialog(onDismissRequest = { editingExam = null }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A2E26)),
                border = BorderStroke(1.2.dp, SaffronYellow.copy(alpha = 0.7f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(text = "${currentExam.code} विवरण संपादित करें", color = PaperLight, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = editTitleHindi, onValueChange = { editTitleHindi = it }, label = { Text("परीक्षा का नाम (हिंदी)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = editBadgeText, onValueChange = { editBadgeText = it }, label = { Text("बैज टैक्स्ट") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = editAuthority, onValueChange = { editAuthority = it }, label = { Text("परीक्षा प्राधिकरण") }, modifier = Modifier.fillMaxWidth())

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { editingExam = null }, modifier = Modifier.weight(1f)) {
                            Text("रद्द करें", color = PaperLight)
                        }

                        Button(
                            onClick = {
                                exams = exams.map {
                                    if (it.id == currentExam.id) it.copy(
                                        titleHindi = editTitleHindi.trim(),
                                        badgeText = editBadgeText.trim(),
                                        stateAuthority = editAuthority.trim()
                                    ) else it
                                }
                                editingExam = null
                                Toast.makeText(context, "${currentExam.code} अपडेट हुआ!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronYellow, contentColor = DeepGreenDark),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("सुरक्षित करें", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // ================= EDIT SOCIAL LINK DIALOG =================
    editingSocialLink?.let { currentLink ->
        var editLabel by remember { mutableStateOf(currentLink.label) }
        var editUrl by remember { mutableStateOf(currentLink.url) }

        Dialog(onDismissRequest = { editingSocialLink = null }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A2E26)),
                border = BorderStroke(1.2.dp, SaffronYellow.copy(alpha = 0.7f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SocialBrandIcon(platform = currentLink.platform, sizeDp = 32.dp)
                        Text(text = "${currentLink.platform} लिंक बदलें", color = PaperLight, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = editLabel, onValueChange = { editLabel = it }, label = { Text("चैनल नाम (Label)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = editUrl, onValueChange = { editUrl = it }, label = { Text("लिंक URL (https://...)") }, modifier = Modifier.fillMaxWidth())

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { editingSocialLink = null }, modifier = Modifier.weight(1f)) {
                            Text("रद्द करें", color = PaperLight)
                        }

                        Button(
                            onClick = {
                                socialLinks = socialLinks.map {
                                    if (it.platform == currentLink.platform) it.copy(label = editLabel.trim(), url = editUrl.trim()) else it
                                }
                                editingSocialLink = null
                                Toast.makeText(context, "${currentLink.platform} लिंक अपडेट हुआ!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronYellow, contentColor = DeepGreenDark),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("सुरक्षित करें", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // ================= ADD NOTICE DIALOG =================
    if (showAddNoticeDialog) {
        var newTitle by remember { mutableStateOf("") }
        var newMessage by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showAddNoticeDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A2E26)),
                border = BorderStroke(1.2.dp, SaffronYellow.copy(alpha = 0.7f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(text = "नया नोटिस लिखें", color = PaperLight, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(value = newTitle, onValueChange = { newTitle = it }, label = { Text("शीर्षक (Title)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = newMessage, onValueChange = { newMessage = it }, label = { Text("संदेश (Message)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { showAddNoticeDialog = false }, modifier = Modifier.weight(1f)) {
                            Text("रद्द करें", color = PaperLight)
                        }

                        Button(
                            onClick = {
                                if (newTitle.isNotBlank() && newMessage.isNotBlank()) {
                                    val item = Announcement(
                                        id = "ann_${System.currentTimeMillis()}",
                                        title = newTitle.trim(),
                                        message = newMessage.trim(),
                                        dateText = "अभी",
                                        isHot = true,
                                        isPublished = true
                                    )
                                    announcements = listOf(item) + announcements
                                    showAddNoticeDialog = false
                                    triggerPublish()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronYellow, contentColor = DeepGreenDark),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("जोड़ें व पब्लिश", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // ================= CLOUD SETTINGS DIALOG =================
    if (showCloudSettingsDialog) {
        var owner by remember { mutableStateOf(gitHubConfig.owner) }
        var repo by remember { mutableStateOf(gitHubConfig.repo) }
        var branch by remember { mutableStateOf(gitHubConfig.branch) }
        var path by remember { mutableStateOf(gitHubConfig.path) }
        var token by remember { mutableStateOf(gitHubConfig.token) }

        Dialog(onDismissRequest = { showCloudSettingsDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A2E26)),
                border = BorderStroke(1.2.dp, SaffronYellow.copy(alpha = 0.7f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(text = "⚙ GitHub / क्लाउड सेटिंग्स", color = PaperLight, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(value = owner, onValueChange = { owner = it }, label = { Text("Repo Owner / Username") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = repo, onValueChange = { repo = it }, label = { Text("Repository Name") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = branch, onValueChange = { branch = it }, label = { Text("Branch") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = path, onValueChange = { path = it }, label = { Text("File Path") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = token, onValueChange = { token = it }, label = { Text("GitHub Token (PAT)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { showCloudSettingsDialog = false }, modifier = Modifier.weight(1f)) {
                            Text("बंद करें", color = PaperLight)
                        }

                        Button(
                            onClick = {
                                val updated = AdminRemoteSyncManager.GitHubConfig(
                                    owner = owner.trim(),
                                    repo = repo.trim(),
                                    branch = branch.trim(),
                                    path = path.trim(),
                                    token = token.trim()
                                )
                                AdminRemoteSyncManager.saveGitHubConfig(context, updated)
                                gitHubConfig = updated
                                showCloudSettingsDialog = false
                                Toast.makeText(context, "क्लाउड सेटिंग्स सुरक्षित!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronYellow, contentColor = DeepGreenDark),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("सहेजें", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
