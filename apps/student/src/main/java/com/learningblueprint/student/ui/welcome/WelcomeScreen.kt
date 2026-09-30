package com.learningblueprint.student.ui.welcome

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.learningblueprint.core.model.Announcement
import com.learningblueprint.core.model.AppConfig
import com.learningblueprint.core.theme.*
import com.learningblueprint.student.sync.StudentSyncManager
import kotlinx.coroutines.launch

@Composable
fun WelcomeScreen(
    initialConfig: AppConfig = AppConfig.DEFAULT,
    onStartClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val uriHandler = LocalUriHandler.current

    var showNotificationDialog by remember { mutableStateOf(false) }
    var showSyncUrlDialog by remember { mutableStateOf(false) }
    var isSyncing by remember { mutableStateOf(false) }
    var syncStatusText by remember { mutableStateOf("ऑफ़लाइन तैयार") }

    // Start with cached config immediately (Zero latency)
    var liveConfig by remember { mutableStateOf(StudentSyncManager.getCachedConfig(context)) }

    fun performSync(showToast: Boolean = false) {
        coroutineScope.launch {
            isSyncing = true
            val (updatedConfig, message) = StudentSyncManager.syncAll(context)
            liveConfig = updatedConfig
            syncStatusText = message
            isSyncing = false
            if (showToast) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(Unit) {
        performSync(showToast = false)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "emblemSpin")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 24000, easing = LinearEasing)
        ),
        label = "rotation"
    )

    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -3.5f,
        targetValue = 3.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0D3A31), DeepGreenDark)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sync status chip (tap to configure custom remote URL)
                Surface(
                    shape = RoundedCornerShape(99.dp),
                    color = Color(0x22FFFFFF),
                    border = BorderStroke(1.dp, SaffronYellow.copy(alpha = 0.4f)),
                    modifier = Modifier.clickable { showSyncUrlDialog = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (StudentSyncManager.isNetworkAvailable(context)) EmeraldGreen else SaffronYellow)
                        )
                        Text(
                            text = if (isSyncing) "सिंक जारी..." else syncStatusText,
                            color = PaperLight,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "शिक्षक पात्रता मंच",
                        color = Color(0xFF8FC3B4),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    // Bell Notification Button with Alert Badge
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .background(Color(0x22FFFFFF))
                            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(11.dp))
                            .clickable {
                                performSync(showToast = false)
                                showNotificationDialog = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(19.dp)) {
                            val w = size.width
                            val h = size.height
                            val bellPath = Path().apply {
                                moveTo(w * 0.5f, h * 0.15f)
                                cubicTo(w * 0.28f, h * 0.15f, w * 0.25f, h * 0.55f, w * 0.15f, h * 0.72f)
                                lineTo(w * 0.85f, h * 0.72f)
                                cubicTo(w * 0.75f, h * 0.55f, w * 0.72f, h * 0.15f, w * 0.5f, h * 0.15f)
                                close()
                            }
                            drawPath(path = bellPath, color = SaffronYellow, style = Stroke(width = 1.8f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                            drawArc(color = SaffronYellow, startAngle = 0f, sweepAngle = 180f, useCenter = false, topLeft = Offset(w * 0.38f, h * 0.76f), size = androidx.compose.ui.geometry.Size(w * 0.24f, h * 0.18f), style = Stroke(width = 1.8f))
                        }

                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 5.dp, end = 5.dp)
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(VermilionRed)
                                .border(1.5.dp, DeepGreenDark, CircleShape)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Text(
                    text = "प्रश्न ✦ उत्तर ✦ अभ्यास",
                    fontFamily = KalamFontFamily,
                    fontSize = 14.sp,
                    color = Color(0x99FFE6B0),
                    modifier = Modifier.graphicsLayer { rotationZ = 5f }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Royal Emblem
            Box(modifier = Modifier.size(124.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(122.dp).graphicsLayer { rotationZ = rotationAngle }) {
                    drawCircle(color = SaffronYellow.copy(alpha = 0.55f), radius = size.minDimension / 2f - 2f, style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 12f), 0f)))
                }

                Box(modifier = Modifier.size(92.dp).graphicsLayer { translationY = floatOffset }, contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val radius = size.minDimension / 2f - 3f

                        drawCircle(brush = Brush.radialGradient(colors = listOf(Color(0xFF134B40), Color(0xFF082720)), center = Offset(cx, cy), radius = radius), radius = radius)
                        drawCircle(color = SaffronYellow, radius = radius, style = Stroke(width = 2.6f))
                        drawCircle(color = SaffronYellow.copy(alpha = 0.35f), radius = radius - 6f, style = Stroke(width = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 6f), 0f)))

                        val topApexY = cy - 24f
                        val leftArmX = cx - 16f
                        val rightArmX = cx + 16f
                        val armBottomY = cy + 22f

                        drawLine(color = Color(0xFFF4EFE1), start = Offset(cx, topApexY), end = Offset(leftArmX, armBottomY), strokeWidth = 3.2f, cap = StrokeCap.Round)
                        drawLine(color = Color(0xFFF4EFE1), start = Offset(cx, topApexY), end = Offset(rightArmX, armBottomY), strokeWidth = 3.2f, cap = StrokeCap.Round)

                        val arcPath = Path().apply {
                            moveTo(leftArmX + 3f, cy + 12f)
                            quadraticBezierTo(cx, cy + 22f, rightArmX - 3f, cy + 12f)
                        }
                        drawPath(path = arcPath, color = SaffronYellow, style = Stroke(width = 2.5f, cap = StrokeCap.Round))
                        drawCircle(color = SaffronYellow, radius = 5f, center = Offset(cx, topApexY))
                        drawCircle(color = DeepGreenDark, radius = 2.2f, center = Offset(cx, topApexY))
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Title
            Text(
                text = "लर्निंग\nब्लूप्रिंट",
                color = PaperLight,
                fontSize = 46.sp,
                fontFamily = RozhaOneFontFamily,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                lineHeight = 50.sp,
                style = TextStyle(shadow = Shadow(color = Color(0x66000000), offset = Offset(0f, 6f), blurRadius = 3f))
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "LEARNING BLUEPRINT", color = SaffronYellow, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 4.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Your Step-by-Step Study Blueprint\nआपकी चरणबद्ध अध्ययन योजना", color = Color(0xFFCFE0D7), fontSize = 14.sp, textAlign = TextAlign.Center, lineHeight = 22.sp)
            Spacer(modifier = Modifier.height(14.dp))
            Text(text = "— स्वागत है, भावी शिक्षक! —", color = Color(0xFFFFE6B0), fontSize = 20.sp, fontFamily = KalamFontFamily, modifier = Modifier.graphicsLayer { rotationZ = -1.6f })
            Spacer(modifier = Modifier.height(14.dp))

            Surface(color = Color.Transparent, shape = RoundedCornerShape(99.dp), border = BorderStroke(1.dp, Color(0x44FFFFFF))) {
                Text(text = "बिना लॉगिन • बिना रजिस्ट्रेशन • सीधे पढ़ाई", color = Color(0xFF8FC3B4), fontSize = 11.sp, modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                Text(text = "✎ चरणबद्ध तैयारी", fontFamily = KalamFontFamily, fontSize = 14.sp, color = Color(0x99FFE6B0), modifier = Modifier.padding(start = 12.dp).graphicsLayer { rotationZ = -5f })
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Live News Ticker Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        performSync(showToast = false)
                        showNotificationDialog = true
                    },
                color = Color(0x18FFFFFF),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0x28FFFFFF))
            ) {
                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = SaffronYellow, shape = RoundedCornerShape(6.dp)) {
                        Text(text = "🔔 नवीनतम", color = DeepGreenDark, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = liveConfig.tickerAnnouncement.message,
                        color = Color(0xFFFFE6B0),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(text = "देखें →", color = SaffronYellow, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp))
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Social Channels
            Text(text = "संपर्क करें • CONNECT", color = Color(0xFF8FC3B4), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                liveConfig.socialLinks.filter { it.isEnabled }.forEach { social ->
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x1AFFFFFF))
                            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                            .clickable {
                                try { uriHandler.openUri(social.url) } catch (e: Exception) {
                                    Toast.makeText(context, "${social.label} लिंक खोलने में असमर्थ", Toast.LENGTH_SHORT).show()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        SocialBrandIcon(platform = social.platform, sizeDp = 44.dp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Centered START Button
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Button(
                    onClick = onStartClick,
                    modifier = Modifier.widthIn(min = 220.dp, max = 260.dp).height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronYellow, contentColor = DeepGreenDark),
                    shape = RoundedCornerShape(99.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Text(text = "START", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Start", modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = liveConfig.appVersionText, color = Color(0xFF6FA595), fontSize = 10.sp, letterSpacing = 1.sp)
        }
    }

    // Notification Center Dialog
    if (showNotificationDialog) {
        Dialog(onDismissRequest = { showNotificationDialog = false }) {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A2E26)),
                border = BorderStroke(1.2.dp, SaffronYellow.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp).fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = "🔔", fontSize = 18.sp)
                            Column {
                                Text(text = "सूचना केंद्र", color = PaperLight, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = RozhaOneFontFamily)
                                Text(text = "OFFICIAL NOTIFICATIONS", color = SaffronYellow, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp)
                            }
                        }
                        IconButton(onClick = { showNotificationDialog = false }, modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0x22FFFFFF))) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = PaperLight, modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 380.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(liveConfig.announcements.filter { it.isPublished }) { item ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0x1AFFFFFF),
                                border = BorderStroke(1.dp, Color(0x2AFFFFFF)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        if (item.isHot) {
                                            Surface(color = VermilionRed, shape = RoundedCornerShape(99.dp)) {
                                                Text(text = "★ नया अपडेट", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                            }
                                        } else {
                                            Text(text = "आधिकारिक सूचना", color = Color(0xFF8FC3B4), fontSize = 10.sp, fontWeight = FontWeight.Medium)
                                        }
                                        Text(text = item.dateText, color = Color(0xFF8A978F), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(text = item.title, color = PaperLight, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, lineHeight = 18.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = item.message, color = Color(0xFFB9C9C0), fontSize = 11.5.sp, lineHeight = 16.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { performSync(showToast = true) },
                            modifier = Modifier.weight(1f).height(42.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("रिफ्रेश", fontSize = 12.sp)
                        }

                        Button(
                            onClick = { showNotificationDialog = false },
                            modifier = Modifier.weight(1.3f).height(42.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronYellow, contentColor = DeepGreenDark),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(text = "समझ गया • CLOSE", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Remote Sync URL Config Dialog
    if (showSyncUrlDialog) {
        var currentUrl by remember { mutableStateOf(StudentSyncManager.getRemoteSyncUrl(context)) }

        Dialog(onDismissRequest = { showSyncUrlDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A2E26)),
                border = BorderStroke(1.2.dp, SaffronYellow.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(text = "🌐 रिमोट सिंक सेटिंग्स", color = PaperLight, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "अन्य डिवाइस पर एडमिन द्वारा पब्लिश की गई सूचनाएं प्राप्त करने हेतु क्लाउड एंडपॉइंट (GitHub Raw / JSON URL):",
                        color = Color(0xFFCFE0D7),
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = currentUrl,
                        onValueChange = { currentUrl = it },
                        label = { Text("Remote Sync URL") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                StudentSyncManager.setRemoteSyncUrl(context, StudentSyncManager.DEFAULT_REMOTE_URL)
                                currentUrl = StudentSyncManager.DEFAULT_REMOTE_URL
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("रीसेट", fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                StudentSyncManager.setRemoteSyncUrl(context, currentUrl)
                                showSyncUrlDialog = false
                                performSync(showToast = true)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronYellow, contentColor = DeepGreenDark),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("सहेजें व सिंक", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
