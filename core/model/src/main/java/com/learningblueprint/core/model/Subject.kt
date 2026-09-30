package com.learningblueprint.core.model

import org.json.JSONObject

data class Subject(
    val id: String,
    val examId: String,
    val paper: ExamPaper,
    val code: String,
    val titleHindi: String,
    val titleEnglish: String,
    val questionCount: Int = 30,
    val marks: Int = 30,
    val chaptersCount: Int,
    val descriptionHindi: String,
    val badgeText: String = "आधिकारिक पाठ्यक्रम",
    val hexColor: Long = 0xFF1C9E5F,
    val isPublished: Boolean = true
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("examId", examId)
        put("paper", paper.name)
        put("code", code)
        put("titleHindi", titleHindi)
        put("titleEnglish", titleEnglish)
        put("questionCount", questionCount)
        put("marks", marks)
        put("chaptersCount", chaptersCount)
        put("descriptionHindi", descriptionHindi)
        put("badgeText", badgeText)
        put("hexColor", hexColor)
        put("isPublished", isPublished)
    }

    companion object {
        fun fromJson(obj: JSONObject): Subject = Subject(
            id = obj.getString("id"),
            examId = obj.getString("examId"),
            paper = try { ExamPaper.valueOf(obj.getString("paper")) } catch (_: Exception) { ExamPaper.PAPER_1 },
            code = obj.getString("code"),
            titleHindi = obj.getString("titleHindi"),
            titleEnglish = obj.optString("titleEnglish", ""),
            questionCount = obj.optInt("questionCount", 30),
            marks = obj.optInt("marks", 30),
            chaptersCount = obj.optInt("chaptersCount", 10),
            descriptionHindi = obj.optString("descriptionHindi", ""),
            badgeText = obj.optString("badgeText", "आधिकारिक पाठ्यक्रम"),
            hexColor = obj.optLong("hexColor", 0xFF1C9E5F),
            isPublished = obj.optBoolean("isPublished", true)
        )

        fun getSubjectsForExam(examId: String, paper: ExamPaper): List<Subject> {
            val baseCdp = Subject(
                id = "${examId}_cdp",
                examId = examId,
                paper = paper,
                code = "CDP",
                titleHindi = "बाल विकास एवं शिक्षाशास्त्र",
                titleEnglish = "Child Development & Pedagogy",
                questionCount = 30,
                marks = 30,
                chaptersCount = 15,
                descriptionHindi = "विकास की अवधारणा, पियाजे, वायगोत्स्की, कोहलबर्ग, समावेशी शिक्षा व अधिगम सिद्धांत",
                badgeText = "अनिवार्य विषय",
                hexColor = 0xFFF6A91B
            )

            val baseHindi = Subject(
                id = "${examId}_hindi",
                examId = examId,
                paper = paper,
                code = "LANG-1",
                titleHindi = "भाषा 1: हिंदी शिक्षणशास्त्र",
                titleEnglish = "Language I: Hindi & Pedagogy",
                questionCount = 30,
                marks = 30,
                chaptersCount = 10,
                descriptionHindi = "अपठित गद्यांश/पद्यांश, व्याकरण, भाषाई कौशल (LSRW) एवं भाषा शिक्षण विधियां",
                badgeText = "अनिवार्य भाषा",
                hexColor = 0xFF1C9E5F
            )

            val baseLang2 = Subject(
                id = "${examId}_lang2",
                examId = examId,
                paper = paper,
                code = "LANG-2",
                titleHindi = "भाषा 2: संस्कृत / English",
                titleEnglish = "Language II: Sanskrit / English",
                questionCount = 30,
                marks = 30,
                chaptersCount = 10,
                descriptionHindi = "व्याकरण, अपठित बोध, संप्रेषण क्षमता एवं द्वितीय भाषा शिक्षण शास्त्र",
                badgeText = "वैकल्पिक भाषा",
                hexColor = 0xFF0288D1
            )

            return when (paper) {
                ExamPaper.PAPER_1 -> listOf(
                    baseCdp,
                    Subject(
                        id = "${examId}_math_p1",
                        examId = examId,
                        paper = paper,
                        code = "MATH",
                        titleHindi = "गणित व शिक्षणशास्त्र (प्राथमिक)",
                        titleEnglish = "Mathematics & Pedagogy (Primary)",
                        questionCount = 30,
                        marks = 30,
                        chaptersCount = 14,
                        descriptionHindi = "संख्या पद्धति, संक्रियाएं, ज्यामिति, मापन, भार, समय, आयतन एवं गणितीय चिंतन",
                        badgeText = "15 विषय + 15 पेडागॉजी",
                        hexColor = 0xFF8E24AA
                    ),
                    Subject(
                        id = "${examId}_evs_p1",
                        examId = examId,
                        paper = paper,
                        code = "EVS",
                        titleHindi = "पर्यावरण अध्ययन (EVS)",
                        titleEnglish = "Environmental Studies (NCERT)",
                        questionCount = 30,
                        marks = 30,
                        chaptersCount = 12,
                        descriptionHindi = "परिवार व मित्र, भोजन, आश्रय, जल, यात्रा, वस्तुएं जो हम बनाते हैं व EVS पेडागॉजी",
                        badgeText = "NCERT कक्षा 3-5 सार",
                        hexColor = 0xFF2E7D32
                    ),
                    baseHindi,
                    baseLang2
                )

                ExamPaper.PAPER_2 -> listOf(
                    baseCdp,
                    Subject(
                        id = "${examId}_science_math_p2",
                        examId = examId,
                        paper = paper,
                        code = "SCI-MATH",
                        titleHindi = "गणित एवं विज्ञान (उच्च प्राथमिक)",
                        titleEnglish = "Mathematics & Science (Class 6-8)",
                        questionCount = 60,
                        marks = 60,
                        chaptersCount = 22,
                        descriptionHindi = "बीजगणित, ज्यामिति, भौतिक, रसायन, जीव विज्ञान एवं विज्ञान शिक्षण शास्त्र",
                        badgeText = "विज्ञान वर्ग (60 अंक)",
                        hexColor = 0xFFD32F2F
                    ),
                    Subject(
                        id = "${examId}_sst_p2",
                        examId = examId,
                        paper = paper,
                        code = "SST",
                        titleHindi = "सामाजिक अध्ययन / सामाजिक विज्ञान",
                        titleEnglish = "Social Studies / Social Science",
                        questionCount = 60,
                        marks = 60,
                        chaptersCount = 24,
                        descriptionHindi = "इतिहास, भूगोल, सामाजिक व राजनीतिक जीवन (नागरिक शास्त्र) एवं SST पेडागॉजी",
                        badgeText = "कला वर्ग (60 अंक)",
                        hexColor = 0xFFE65100
                    ),
                    baseHindi,
                    baseLang2
                )

                ExamPaper.BOTH -> listOf(
                    baseCdp,
                    Subject(
                        id = "${examId}_math_combo",
                        examId = examId,
                        paper = paper,
                        code = "MATH-ALL",
                        titleHindi = "सम्पूर्ण गणित (प्राथमिक + उच्च प्राथमिक)",
                        titleEnglish = "Complete Mathematics (Paper 1 & 2)",
                        questionCount = 60,
                        marks = 60,
                        chaptersCount = 20,
                        descriptionHindi = "आधारभूत अंकगणित से लेकर उच्च प्राथमिक बीजगणित व शिक्षणशास्त्र का सम्पूर्ण संकलन",
                        badgeText = "कम्बाइंड मॉड्यूल",
                        hexColor = 0xFF8E24AA
                    ),
                    Subject(
                        id = "${examId}_evs_sci_combo",
                        examId = examId,
                        paper = paper,
                        code = "EVS-SCI",
                        titleHindi = "पर्यावरण अध्ययन एवं सामान्य विज्ञान",
                        titleEnglish = "EVS & Elementary Science",
                        questionCount = 60,
                        marks = 60,
                        chaptersCount = 18,
                        descriptionHindi = "NCERT प्राथमिक पर्यावरण एवं कक्षा 6-8 विज्ञान का एकीकृत पाठ्यक्रम",
                        badgeText = "कम्बाइंड मॉड्यूल",
                        hexColor = 0xFF2E7D32
                    ),
                    Subject(
                        id = "${examId}_sst_combo",
                        examId = examId,
                        paper = paper,
                        code = "SST",
                        titleHindi = "सामाजिक अध्ययन (उच्च प्राथमिक कला वर्ग)",
                        titleEnglish = "Social Science Comprehensive",
                        questionCount = 60,
                        marks = 60,
                        chaptersCount = 24,
                        descriptionHindi = "इतिहास, भूगोल, नागरिक शास्त्र एवं शिक्षणशास्त्र",
                        badgeText = "कला वर्ग (60 अंक)",
                        hexColor = 0xFFE65100
                    ),
                    baseHindi,
                    baseLang2
                )
            }
        }
    }
}
