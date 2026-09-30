package com.learningblueprint.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BatchQuestionParserTest {

    @Test
    fun testParseAssertionReasonQuestionWithRichExplanation() {
        val sampleText = """
केन्द्रीय शिक्षक पात्रता परीक्षा (CTET) प्रश्न-पत्र :
बाल विकास एवं शिक्षाशास्त्र (Child Development and Pedagogy)
प्रश्न 1.
अभिकथन (A): बाल विकास एक सार्वभौमिक और अपरिवर्तनीय अनुक्रम का पालन नहीं करता, बल्कि यह सांस्कृतिक रूप से मध्यस्थता प्राप्त और गैर-रैखिक (Non-linear) प्रक्रिया है। कारण (R): विकासशील मस्तिष्क की तंत्रिका-लचीलापन (Neuroplasticity) यह दर्शाती है कि अनुभव विशिष्ट तंत्रिका-संबंधों को सक्रिय रूप से आकार देते हैं, जिससे विभिन्न सामाजिक-सांस्कृतिक परिवेशों में बच्चों के विकासात्मक प्रक्षेपवक्र (Developmental Trajectories) भिन्न हो जाते हैं।
सही विकल्प चुनिए: (1) (A) सही है, लेकिन (R) गलत है। (2) (A) और (R) दोनों गलत हैं। (3) (A) और (R) दोनों सही हैं तथा (R), (A) की सही व्याख्या करता है। (4) (A) और (R) दोनों सही हैं, लेकिन (R), (A) की सही व्याख्या नहीं करता।
सही उत्तर: (3)
विस्तृत व्याख्या: विकासात्मक तंत्रिका-विज्ञान और आधुनिक बाल मनोविज्ञान (जैसे कि लेव वाइगोत्स्की और गतिशील प्रणाली सिद्धांत) यह पुष्ट करते हैं कि मानव विकास कठोर सार्वभौमिक चरणों में बद्ध नहीं है।
गलत विकल्प क्यों गलत हैं:
(1) गलत है क्योंकि (R) विकासात्मक तंत्रिका-विज्ञान का अकादमिक रूप से सिद्ध नियम है।
संबंधित सिद्धांत: गतिशील प्रणाली सिद्धांत
परीक्षा युक्ति / ट्रिक: जब अभिकथन किसी व्यापक विकासात्मक परिदृश्य की बात करे।
सामान्य भ्रम: अभ्यर्थी अक्सर विकास को पूरी तरह 'सार्वभौमिक' मानने की पुरानी परिभाषा में उलझ जाते हैं।
स्मृति संकेत: "मस्तिष्क + संस्कृति = विविधतापूर्ण प्रक्षेपवक्र"।
कठिनाई स्तर: अत्यधिक कठिन
ब्लूम स्तर: मूल्यांकन
परीक्षित दक्षता: विकास के जैविक-सांस्कृतिक सिद्धांतों का समाकलित विश्लेषण।
सीटेट पूर्व प्रवृत्ति: विगत वर्षों में पियाजे के सार्वभौमिकता बनाम सांस्कृतिक संदर्भ पर प्रश्न बढ़े हैं।

प्रश्न 2.
सात वर्षीय आरव के सामने मिट्टी की दो समान गोलाकार गेंदें रखी गईं। उसके सामने ही शिक्षिका ने एक गेंद को चपटा करके रोटी जैसा बना दिया। आरव कहता है कि "चपटी रोटी में मिट्टी अधिक है क्योंकि यह मेज़ पर ज़्यादा जगह घेर रही है।" आरव के इस तर्क का प्राथमिक संज्ञानात्मक कारण क्या है? (1) आनुपातिक तर्क का पूर्ण अभाव (2) केंद्रीयकरण और अपलटावीपन (3) परावर्तित चिंतन की सक्रियता (4) सांकेतिक प्रकार्यता का अविकसित होना
सही उत्तर: (2)
विस्तृत व्याख्या: जीन पियाजे के अनुसार, पूर्व-संक्रियात्मक अवस्था के अंत में बच्चे संरक्षण के संप्रत्यय को पूरी तरह नहीं समझ पाते।
        """.trimIndent()

        val result = BatchQuestionParser.parseRawText(
            rawText = sampleText,
            chapterId = "cdp_ch01",
            subjectCode = "CDP",
            defaultExamYear = "CTET Official PYQ"
        )

        assertEquals("Detected questions count mismatch", 2, result.totalDetected)
        assertEquals("Failed count must be 0", 0, result.failedCount)
        assertEquals("Parsed questions count mismatch", 2, result.successfulQuestions.size)

        val q1 = result.successfulQuestions[0]
        assertTrue("Question text must contain Assertion (A)", q1.questionHindi.contains("अभिकथन (A)"))
        assertTrue("Question text must contain Reason (R)", q1.questionHindi.contains("कारण (R)"))
        assertEquals("Must have exactly 4 options", 4, q1.options.size)
        assertEquals("Correct option index must be 2 (Option 3)", 2, q1.correctOptionIndex)
        assertTrue("Explanation must preserve pedagogical analysis", q1.explanationHindi.contains("गलत विकल्प क्यों गलत हैं"))
        assertTrue("Explanation must preserve memory cue", q1.explanationHindi.contains("स्मृति संकेत"))

        val q2 = result.successfulQuestions[1]
        assertEquals("Option 2 should be index 1", 1, q2.correctOptionIndex)
        assertTrue("Option 2 text must match", q2.options[1].contains("केंद्रीयकरण और अपलटावीपन"))
    }
}
