package com.learningblueprint.core.model

enum class ExamPaper(val label: String, val subtitle: String) {
    PAPER_1("पेपर 1 (प्राथमिक)", "कक्षा 1 से 5 हेतु"),
    PAPER_2("पेपर 2 (उच्च प्राथमिक)", "कक्षा 6 से 8 हेतु"),
    BOTH("दोनों पेपर (1 & 2)", "सम्पूर्ण तैयारी ब्लूप्रिंट")
}

data class Exam(
    val id: String,
    val code: String,
    val titleHindi: String,
    val titleEnglish: String,
    val stateAuthority: String,
    val badgeText: String,
    val isHot: Boolean = false,
    val availablePapers: List<ExamPaper> = listOf(ExamPaper.PAPER_1, ExamPaper.PAPER_2, ExamPaper.BOTH),
    val totalSubjects: Int,
    val totalQuestions: Int = 150,
    val totalMarks: Int = 150,
    val durationMinutes: Int = 150
) {
    companion object {
        val ALL_EXAMS: List<Exam> = listOf(
            Exam(
                id = "ctet",
                code = "CTET",
                titleHindi = "केंद्रीय शिक्षक पात्रता परीक्षा",
                titleEnglish = "Central Teacher Eligibility Test",
                stateAuthority = "CBSE • अखिल भारतीय",
                badgeText = "★ सत्र 2026 लाइव",
                isHot = true,
                totalSubjects = 5,
                totalQuestions = 150,
                totalMarks = 150,
                durationMinutes = 150
            ),
            Exam(
                id = "uptet",
                code = "UPTET",
                titleHindi = "उत्तर प्रदेश शिक्षक पात्रता परीक्षा",
                titleEnglish = "UP Teacher Eligibility Test",
                stateAuthority = "UP DElEd • उत्तर प्रदेश",
                badgeText = "नया पाठ्यक्रम",
                isHot = true,
                totalSubjects = 5,
                totalQuestions = 150,
                totalMarks = 150,
                durationMinutes = 150
            ),
            Exam(
                id = "supertet",
                code = "SUPER TET",
                titleHindi = "उत्तर प्रदेश प्राथमिक शिक्षक भर्ती",
                titleEnglish = "UP Assistant Teacher Recruitment",
                stateAuthority = "UP Basic Education Board",
                badgeText = "भर्ती परीक्षा",
                isHot = false,
                totalSubjects = 14,
                totalQuestions = 150,
                totalMarks = 150,
                durationMinutes = 150
            ),
            Exam(
                id = "reet",
                code = "REET",
                titleHindi = "राजस्थान अध्यापक पात्रता परीक्षा",
                titleEnglish = "Rajasthan Eligibility Exam for Teachers",
                stateAuthority = "RBSE • राजस्थान",
                badgeText = "लेवल 1 व 2",
                isHot = false,
                totalSubjects = 5,
                totalQuestions = 150,
                totalMarks = 150,
                durationMinutes = 150
            ),
            Exam(
                id = "mptet",
                code = "MPTET",
                titleHindi = "मध्य प्रदेश प्राथमिक शिक्षक पात्रता",
                titleEnglish = "MP Teacher Eligibility Test (Varg 3)",
                stateAuthority = "MP PEB • मध्य प्रदेश",
                badgeText = "वर्ग 3",
                isHot = false,
                totalSubjects = 5,
                totalQuestions = 150,
                totalMarks = 150,
                durationMinutes = 150
            ),
            Exam(
                id = "stet",
                code = "BIHAR STET",
                titleHindi = "बिहार माध्यमिक शिक्षक पात्रता",
                titleEnglish = "Bihar Secondary Teacher Eligibility",
                stateAuthority = "BSEB • बिहार",
                badgeText = "माध्यमिक स्तर",
                isHot = false,
                totalSubjects = 5,
                totalQuestions = 150,
                totalMarks = 150,
                durationMinutes = 150
            )
        )
    }
}
