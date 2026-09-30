package com.learningblueprint.core.model

import org.json.JSONArray
import org.json.JSONObject

data class SocialLink(
    val platform: String,
    val label: String,
    val url: String,
    val hexColor: Long,
    val isEnabled: Boolean = true
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("platform", platform)
        put("label", label)
        put("url", url)
        put("hexColor", hexColor)
        put("isEnabled", isEnabled)
    }

    companion object {
        fun fromJson(obj: JSONObject): SocialLink = SocialLink(
            platform = obj.getString("platform"),
            label = obj.getString("label"),
            url = obj.getString("url"),
            hexColor = obj.optLong("hexColor", 0xFFFFFFFF),
            isEnabled = obj.optBoolean("isEnabled", true)
        )
    }
}

data class Announcement(
    val id: String,
    val title: String,
    val message: String,
    val dateText: String,
    val actionUrl: String? = null,
    val isHot: Boolean = false,
    val isPublished: Boolean = true
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("title", title)
        put("message", message)
        put("dateText", dateText)
        put("actionUrl", actionUrl ?: "")
        put("isHot", isHot)
        put("isPublished", isPublished)
    }

    companion object {
        fun fromJson(obj: JSONObject): Announcement = Announcement(
            id = obj.getString("id"),
            title = obj.getString("title"),
            message = obj.getString("message"),
            dateText = obj.optString("dateText", "नवीनतम"),
            actionUrl = obj.optString("actionUrl").takeIf { it.isNotBlank() },
            isHot = obj.optBoolean("isHot", false),
            isPublished = obj.optBoolean("isPublished", true)
        )
    }
}

data class AppConfig(
    val tickerAnnouncement: Announcement,
    val announcements: List<Announcement>,
    val socialLinks: List<SocialLink>,
    val appVersionText: String = "v4.0 • PRODUCTION BUILD",
    val lastSyncTime: Long = System.currentTimeMillis()
) {
    fun toJsonString(): String {
        val root = JSONObject()
        val noticeArray = JSONArray()
        announcements.forEach { noticeArray.put(it.toJson()) }
        root.put("announcements", noticeArray)

        val socialArray = JSONArray()
        socialLinks.forEach { socialArray.put(it.toJson()) }
        root.put("socialLinks", socialArray)

        root.put("appVersionText", appVersionText)
        root.put("lastSyncTime", lastSyncTime)
        return root.toString(2)
    }

    companion object {
        val DEFAULT = AppConfig(
            tickerAnnouncement = Announcement(
                id = "ann_01",
                title = "CTET 2026",
                message = "CTET सत्र 2026: आवेदन प्रक्रिया प्रारंभ • सम्पूर्ण ब्लूप्रिंट लाइव!",
                dateText = "आज",
                isHot = true
            ),
            announcements = listOf(
                Announcement(
                    id = "ann_01",
                    title = "CTET दिसंबर सत्र 2026: आवेदन प्रारंभ",
                    message = "केंद्रीय शिक्षक पात्रता परीक्षा हेतु आधिकारिक विवरणिका जारी। पाठ्यक्रम के अनुसार विषयवार चरणबद्ध तैयारी शुरू करें।",
                    dateText = "आज",
                    isHot = true
                ),
                Announcement(
                    id = "ann_02",
                    title = "नई फुल-लेंथ मॉक टेस्ट सीरीज़ लाइव हुई",
                    message = "पेपर 1 और पेपर 2 के लिए नवीनतम परीक्षा पैटर्न पर आधारित 150-प्रश्नों वाले मॉक टेस्ट अभ्यास हेतु उपलब्ध हैं।",
                    dateText = "कल",
                    isHot = true
                ),
                Announcement(
                    id = "ann_03",
                    title = "बाल विकास (CDP) में 120+ नए PYQ व्याख्या सहित जुड़े",
                    message = "पियाजे, वायगोत्स्की और कोहलबर्ग के सिद्धांतों पर विगत वर्षों के प्रश्न विस्तृत हल सहित जोड़े गए हैं।",
                    dateText = "2 दिन पहले",
                    isHot = false
                ),
                Announcement(
                    id = "ann_04",
                    title = "टेलीग्राम व व्हाट्सएप स्टडी ग्रुप से जुड़ें",
                    message = "दैनिक महत्वपूर्ण प्रश्न, पीडीएफ नोट्स और परीक्षा सूचनाओं के लिए आधिकारिक शिक्षक मंच से जुड़े रहें।",
                    dateText = "5 दिन पहले",
                    isHot = false
                )
            ),
            socialLinks = listOf(
                SocialLink("WA", "WhatsApp", "https://whatsapp.com", 0xFF25D366),
                SocialLink("YT", "YouTube", "https://youtube.com", 0xFFFF4E45),
                SocialLink("X", "X", "https://x.com", 0xFFE2E8F0),
                SocialLink("FB", "Facebook", "https://facebook.com", 0xFF4267B2),
                SocialLink("IG", "Instagram", "https://instagram.com", 0xFFE1306C)
            )
        )

        fun fromJsonString(jsonString: String): AppConfig? {
            return try {
                val root = JSONObject(jsonString)
                val noticeList = mutableListOf<Announcement>()
                val noticeArr = root.optJSONArray("announcements")
                if (noticeArr != null) {
                    for (i in 0 until noticeArr.length()) {
                        noticeList.add(Announcement.fromJson(noticeArr.getJSONObject(i)))
                    }
                }

                val socialList = mutableListOf<SocialLink>()
                val socialArr = root.optJSONArray("socialLinks")
                if (socialArr != null) {
                    for (i in 0 until socialArr.length()) {
                        socialList.add(SocialLink.fromJson(socialArr.getJSONObject(i)))
                    }
                }

                val liveNotices = noticeList.filter { it.isPublished }
                val ticker = liveNotices.firstOrNull() ?: DEFAULT.tickerAnnouncement

                AppConfig(
                    tickerAnnouncement = ticker,
                    announcements = if (liveNotices.isNotEmpty()) liveNotices else DEFAULT.announcements,
                    socialLinks = if (socialList.isNotEmpty()) socialList else DEFAULT.socialLinks,
                    appVersionText = root.optString("appVersionText", DEFAULT.appVersionText),
                    lastSyncTime = root.optLong("lastSyncTime", System.currentTimeMillis())
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}
