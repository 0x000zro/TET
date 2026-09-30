package com.learningblueprint.admin.ui

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Send
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
import com.learningblueprint.core.model.Announcement
import com.learningblueprint.core.model.AppConfig
import com.learningblueprint.core.model.Exam
import com.learningblueprint.core.model.SocialLink
import com.learningblueprint.core.theme.*
import kotlinx.coroutines.launch

@Composable
fun AdminDashboardScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(0) }

    var announcements by remember { mutableStateOf(AppConfig.DEFAULT.announcements) }
    var socialLinks by remember { mutableStateOf(AppConfig.DEFAULT.socialLinks) }
    var exams by remember { mutableStateOf(AppConfig.DEFAULT.exams) }
    var isPublishing by remember { mutableStateOf(false) }

    var showAddNoticeDialog by remember { mutableStateOf(false) }
    var editingSocialLink by remember { mutableStateOf<SocialLink?>(null) }
    var editingExam by remember { mutableStateOf<Exam?>(null) }
    var showCloudSettingsDialog by remember { mutableStateOf(false) }
    var gitHubConfig by remember { mutableStateOf(AdminRemoteSyncManager.getGitHubConfig(context)) }

    fun triggerPublish() {
        coroutineScope.launch {
            isPublishing = true
            val liveNotices = announcements.filter { it.isPublished }
            val ticker = liveNotices.firstOrNull() ?: AppConfig.DEFAULT.tickerAnnouncement
            val config = AppConfig(
                tickerAnnouncement = ticker,
                announcements = announcements,
                socialLinks = socialLinks,
                exams = exams,
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
                        text = "कंटेंट एवं सिस्टम कंट्रोल सेंटर",
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
                        Icon(Icons.Default.Send, contentDescription = "Publish", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = if (isPublishing) "पब्लिशिंग..." else "पब्लिश", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ================= 4-TAB NAVIGATION BAR =================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x18FFFFFF))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("🔔 सूचना", "🌐 लिंक्स", "📚 परीक्षा", "📱 सिंक").forEachIndexed { index, title ->
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
                            fontSize = 11.5.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            when (selectedTab) {
                0 -> {
                    // TAB 1: NOTICES
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "कुल सूचनाएं (${announcements.size})",
                            color = PaperLight,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )

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
                                                onClick = {
                                                    announcements = announcements.filter { it.id != notice.id }
                                                },
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
                    // TAB 2: SOCIAL LINKS
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
                                            Text(
                                                text = link.label,
                                                color = PaperLight,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = link.url,
                                                color = Color(0xFF8FC3B4),
                                                fontSize = 11.sp,
                                                maxLines = 1
                                            )
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
                    // TAB 3: EXAM MANAGEMENT (SCREEN 2 ADMIN CONTROLS)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "शिक्षक पात्रता परीक्षा प्रबंधन (${exams.size})",
                                color = PaperLight,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "छात्र ऐप में परीक्षा लाइव करने या छिपाने हेतु टॉगल करें",
                                color = Color(0xFF8FC3B4),
                                fontSize = 11.sp
                            )
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
                                border = BorderStroke(
                                    1.2.dp,
                                    if (exam.isPublished) SaffronYellow.copy(alpha = 0.5f) else Color(0x22FFFFFF)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
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
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (exam.isPublished) SaffronYellow else Color(0x33FFFFFF)
                                            ) {
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

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Button(
                                                onClick = { editingExam = exam },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = SaffronYellow,
                                                    contentColor = DeepGreenDark
                                                ),
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

                                    Text(
                                        text = exam.titleHindi,
                                        color = PaperLight,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Text(
                                        text = exam.stateAuthority,
                                        color = Color(0xFF8FC3B4),
                                        fontSize = 11.sp
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
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

                                        Text(
                                            text = "हॉट बैज:",
                                            color = Color(0xFF8FC3B4),
                                            fontSize = 10.5.sp
                                        )

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
                                text = "अब आप किसी भी दूसरे फोन पर मौजूद स्टूडेंट ऐप में सीधे परीक्षा, नोटिस व सोशल लिंक भेज सकते हैं।",
                                color = Color(0xFFCFE0D7),
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "• GitHub रिपॉजिटरी: ${gitHubConfig.owner}/${gitHubConfig.repo} (${gitHubConfig.branch})",
                                color = Color(0xFF1C9E5F),
                                fontSize = 12.5.sp
                            )
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
                    Text(
                        text = "${currentExam.code} विवरण संपादित करें",
                        color = PaperLight,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = editTitleHindi,
                        onValueChange = { editTitleHindi = it },
                        label = { Text("परीक्षा का नाम (हिंदी)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = editBadgeText,
                        onValueChange = { editBadgeText = it },
                        label = { Text("बैज टैक्स्ट (e.g. ★ सत्र 2026 लाइव)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = editAuthority,
                        onValueChange = { editAuthority = it },
                        label = { Text("परीक्षा प्राधिकरण (Authority)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { editingExam = null },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
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
                                Toast.makeText(context, "${currentExam.code} अपडेट हुआ! पब्लिश करें दबाएँ।", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronYellow, contentColor = DeepGreenDark),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("सुरक्षित करें", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // ================= RESTORED EDIT SOCIAL LINK DIALOG =================
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
                        Text(
                            text = "${currentLink.platform} लिंक बदलें",
                            color = PaperLight,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = editLabel,
                        onValueChange = { editLabel = it },
                        label = { Text("चैनल नाम (Label)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = editUrl,
                        onValueChange = { editUrl = it },
                        label = { Text("लिंक URL (https://...)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { editingSocialLink = null },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("रद्द करें", color = PaperLight)
                        }

                        Button(
                            onClick = {
                                socialLinks = socialLinks.map {
                                    if (it.platform == currentLink.platform) it.copy(
                                        label = editLabel.trim(),
                                        url = editUrl.trim()
                                    ) else it
                                }
                                editingSocialLink = null
                                Toast.makeText(context, "${currentLink.platform} लिंक अपडेट हुआ! पब्लिश करें दबाएँ।", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronYellow, contentColor = DeepGreenDark),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
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
        var isHot by remember { mutableStateOf(true) }

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

                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("शीर्षक (Title)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newMessage,
                        onValueChange = { newMessage = it },
                        label = { Text("संदेश (Message)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
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
                                        isHot = isHot,
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
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "अन्य फोन पर ऑटो-सिंक हेतु अपनी GitHub रिपॉजिटरी कॉन्फ़िगर करें:", color = Color(0xFF8FC3B4), fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(value = owner, onValueChange = { owner = it }, label = { Text("Repo Owner / Username") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = repo, onValueChange = { repo = it }, label = { Text("Repository Name") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = branch, onValueChange = { branch = it }, label = { Text("Branch (e.g. main)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = path, onValueChange = { path = it }, label = { Text("File Path (e.g. announcements.json)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = token, onValueChange = { token = it }, label = { Text("GitHub Token (PAT with repo write)") }, modifier = Modifier.fillMaxWidth())

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
