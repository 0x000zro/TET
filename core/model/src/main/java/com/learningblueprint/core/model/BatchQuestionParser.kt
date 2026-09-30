package com.learningblueprint.core.model

import java.util.UUID

object BatchQuestionParser {

    data class ParseResult(
        val successfulQuestions: List<Question>,
        val totalDetected: Int,
        val failedCount: Int,
        val errors: List<String>
    )

    private data class ExtractedContent(
        val questionText: String,
        val options: List<String>
    )

    /**
     * Parses raw pasted text containing 1 to 150+ questions into structured Question objects.
     */
    fun parseRawText(
        rawText: String,
        chapterId: String,
        subjectCode: String,
        defaultExamYear: String = "CTET Official PYQ"
    ): ParseResult {
        if (rawText.isBlank()) {
            return ParseResult(emptyList(), 0, 0, listOf("पेस्ट किया गया टेक्स्ट रिक्त है।"))
        }

        val questions = mutableListOf<Question>()
        val errors = mutableListOf<String>()

        // Split on question boundaries: "प्रश्न 1.", "प्रश्न 1:", "Q1."
        val questionSplitter = Regex("""(?m)(?:^|\n)\s*(?:प्रश्न|Q\.?)\s*(\d+)[\.\:]?\s*""")
        val matches = questionSplitter.findAll(rawText).toList()

        if (matches.isEmpty()) {
            return ParseResult(
                emptyList(), 0, 1,
                listOf("कोई प्रश्न नहीं मिला। कृपया सुनिश्चित करें कि प्रत्येक प्रश्न 'प्रश्न 1.', 'प्रश्न 2.' से शुरू होता है।")
            )
        }

        for (i in matches.indices) {
            val currentMatch = matches[i]
            val questionNumber = currentMatch.groupValues[1]
            val startIndex = currentMatch.range.last + 1
            val endIndex = if (i + 1 < matches.size) matches[i + 1].range.first else rawText.length

            val block = rawText.substring(startIndex, endIndex).trim()

            try {
                val parsedQuestion = parseSingleBlock(
                    qNum = questionNumber,
                    block = block,
                    chapterId = chapterId,
                    subjectCode = subjectCode,
                    defaultExamYear = defaultExamYear
                )
                if (parsedQuestion != null) {
                    questions.add(parsedQuestion)
                } else {
                    errors.add("प्रश्न $questionNumber: 4 विकल्प अथवा सही उत्तर कुंजी नहीं मिल सकी।")
                }
            } catch (e: Exception) {
                errors.add("प्रश्न $questionNumber: पार्सिंग त्रुटि - ${e.message}")
            }
        }

        return ParseResult(
            successfulQuestions = questions,
            totalDetected = matches.size,
            failedCount = matches.size - questions.size,
            errors = errors
        )
    }

    private fun parseSingleBlock(
        qNum: String,
        block: String,
        chapterId: String,
        subjectCode: String,
        defaultExamYear: String
    ): Question? {
        // 1. Extract Answer Key: "सही उत्तर: (3)" or "सही उत्तर: 3"
        val answerRegex = Regex("""(?m)(?:सही\s*)?उत्तर\s*[:\-]?\s*[\(\[]?([1-4A-Da-dअ-द])[\)\]]?""")
        val answerMatch = answerRegex.find(block) ?: return null
        val rawAnswerKey = answerMatch.groupValues[1]

        val correctIndex = when (rawAnswerKey) {
            "1", "A", "a", "अ" -> 0
            "2", "B", "b", "ब" -> 1
            "3", "C", "c", "स" -> 2
            "4", "D", "d", "द" -> 3
            else -> 0
        }

        val answerIndexInBlock = answerMatch.range.first

        // 2. Separate Question+Options from Explanation
        val preAnswerText = block.substring(0, answerIndexInBlock).trim()
        val postAnswerText = block.substring(answerMatch.range.last + 1).trim()

        // 3. Extract Full Pedagogical Explanation
        val explanationHindi = postAnswerText
            .replace(Regex("""^(?:विस्तृत\s*)?व्याख्या\s*[:\-]?\s*"""), "")
            .trim()

        // 4. Sequential 4-Option Extraction (Avoids confusing Assertion (A) with Option A)
        val extracted = extractQuestionAndOptions(preAnswerText) ?: return null

        val qId = "q_${chapterId}_${qNum.padStart(3, '0')}_${UUID.randomUUID().toString().take(6)}"

        return Question(
            id = qId,
            chapterId = chapterId,
            subjectCode = subjectCode,
            examYearText = defaultExamYear,
            questionHindi = extracted.questionText,
            questionEnglish = "",
            options = extracted.options,
            correctOptionIndex = correctIndex,
            explanationHindi = explanationHindi
        )
    }

    private fun extractQuestionAndOptions(preAnswerText: String): ExtractedContent? {
        // Priority 1: Sequential (1), (2), (3), (4)
        val reg1 = Regex("""(?:\(|\[|\b)1(?:\)|\.|\-|\s)\s*""")
        val reg2 = Regex("""(?:\(|\[|\b)2(?:\)|\.|\-|\s)\s*""")
        val reg3 = Regex("""(?:\(|\[|\b)3(?:\)|\.|\-|\s)\s*""")
        val reg4 = Regex("""(?:\(|\[|\b)4(?:\)|\.|\-|\s)\s*""")

        for (m1 in reg1.findAll(preAnswerText)) {
            val end1 = m1.range.last + 1
            val m2 = reg2.find(preAnswerText, end1) ?: continue
            val end2 = m2.range.last + 1
            val m3 = reg3.find(preAnswerText, end2) ?: continue
            val end3 = m3.range.last + 1
            val m4 = reg4.find(preAnswerText, end3) ?: continue
            val end4 = m4.range.last + 1

            val qText = preAnswerText.substring(0, m1.range.first)
                .replace(Regex("""सही विकल्प चुनिए\s*[:\-]?\s*$"""), "")
                .replace(Regex("""सही कूट चुनिए\s*[:\-]?\s*$"""), "")
                .trim()

            val opt1 = preAnswerText.substring(end1, m2.range.first).trim()
            val opt2 = preAnswerText.substring(end2, m3.range.first).trim()
            val opt3 = preAnswerText.substring(end3, m4.range.first).trim()
            val opt4 = preAnswerText.substring(end4).trim()

            if (opt1.isNotBlank() && opt2.isNotBlank() && opt3.isNotBlank() && opt4.isNotBlank()) {
                return ExtractedContent(qText, listOf(opt1, opt2, opt3, opt4))
            }
        }

        // Priority 2: Sequential (A), (B), (C), (D)
        val regA = Regex("""(?:\(|\[|\b)[Aa](?:\)|\.|\-|\s)\s*""")
        val regB = Regex("""(?:\(|\[|\b)[Bb](?:\)|\.|\-|\s)\s*""")
        val regC = Regex("""(?:\(|\[|\b)[Cc](?:\)|\.|\-|\s)\s*""")
        val regD = Regex("""(?:\(|\[|\b)[Dd](?:\)|\.|\-|\s)\s*""")

        for (mA in regA.findAll(preAnswerText)) {
            val endA = mA.range.last + 1
            val mB = regB.find(preAnswerText, endA) ?: continue
            val endB = mB.range.last + 1
            val mC = regC.find(preAnswerText, endB) ?: continue
            val endC = mC.range.last + 1
            val mD = regD.find(preAnswerText, endC) ?: continue
            val endD = mD.range.last + 1

            val qText = preAnswerText.substring(0, mA.range.first)
                .replace(Regex("""सही विकल्प चुनिए\s*[:\-]?\s*$"""), "")
                .replace(Regex("""सही कूट चुनिए\s*[:\-]?\s*$"""), "")
                .trim()

            val optA = preAnswerText.substring(endA, mB.range.first).trim()
            val optB = preAnswerText.substring(endB, mC.range.first).trim()
            val optC = preAnswerText.substring(endC, mD.range.first).trim()
            val optD = preAnswerText.substring(endD).trim()

            if (optA.isNotBlank() && optB.isNotBlank() && optC.isNotBlank() && optD.isNotBlank()) {
                return ExtractedContent(qText, listOf(optA, optB, optC, optD))
            }
        }

        return null
    }
}
