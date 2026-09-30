package com.learningblueprint.core.model

import org.json.JSONArray
import org.json.JSONObject

data class Question(
    val id: String,
    val chapterId: String,
    val subjectCode: String,
    val examYearText: String,
    val questionHindi: String,
    val questionEnglish: String,
    val options: List<String>,
    val correctOptionIndex: Int,
    val explanationHindi: String
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("chapterId", chapterId)
        put("subjectCode", subjectCode)
        put("examYearText", examYearText)
        put("questionHindi", questionHindi)
        put("questionEnglish", questionEnglish)
        val opts = JSONArray()
        options.forEach { opts.put(it) }
        put("options", opts)
        put("correctOptionIndex", correctOptionIndex)
        put("explanationHindi", explanationHindi)
    }

    companion object {
        fun fromJson(obj: JSONObject): Question {
            val opts = mutableListOf<String>()
            val arr = obj.optJSONArray("options")
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    opts.add(arr.getString(i))
                }
            }
            return Question(
                id = obj.getString("id"),
                chapterId = obj.getString("chapterId"),
                subjectCode = obj.getString("subjectCode"),
                examYearText = obj.optString("examYearText", "CTET PYQ"),
                questionHindi = obj.getString("questionHindi"),
                questionEnglish = obj.optString("questionEnglish", ""),
                options = opts,
                correctOptionIndex = obj.optInt("correctOptionIndex", 0),
                explanationHindi = obj.optString("explanationHindi", "")
            )
        }

        fun getQuestionsForChapter(chapterId: String): List<Question> {
            return when {
                chapterId.startsWith("cdp_ch01") -> cdpCh01Questions
                chapterId.startsWith("cdp_ch02") -> cdpCh02Questions
                chapterId.startsWith("evs_ch01") -> evsCh01Questions
                chapterId.startsWith("math_ch01") -> mathCh01Questions
                chapterId.startsWith("hindi_ch01") -> hindiCh01Questions
                else -> defaultChapterQuestions(chapterId)
            }
        }

        private val cdpCh01Questions = listOf(
            Question(
                id = "q_cdp_01_01",
                chapterId = "cdp_ch01",
                subjectCode = "CDP",
                examYearText = "CTET Jan 2024 • Paper 1",
                questionHindi = "जीन पियाजे के संज्ञानात्मक विकास सिद्धांत के अनुसार, एक 5 वर्ष की बच्ची जो यह मानती है कि उसकी गुड़िया जीवित है और उसे भूख लगती है, वह चिंतन की किस विशेषता को प्रदर्शित कर रही है?",
                questionEnglish = "According to Jean Piaget's cognitive theory, a 5-year-old child believing that her doll is alive demonstrates which characteristic of thinking?",
                options = listOf(
                    "प्रतिवर्ती सोच (Reversible Thinking)",
                    "जीववादी चिंतन (Animistic Thinking)",
                    "अमूर्त तार्किकता (Abstract Logic)",
                    "संरक्षण की क्षमता (Conservation)"
                ),
                correctOptionIndex = 1,
                explanationHindi = "पियाजे के अनुसार पूर्व-संक्रियात्मक अवस्था (2 से 7 वर्ष) में बालक में 'जीववाद' (Animism) पाया जाता है। इसमें बच्चा यह मानता है कि निर्जीव वस्तुएं (कार, गुड़िया, खिलौने) भी सजीव हैं और उनमें मानवीय भावनाएं एवं उद्देश्य होते हैं।"
            ),
            Question(
                id = "q_cdp_01_02",
                chapterId = "cdp_ch01",
                subjectCode = "CDP",
                examYearText = "CTET Aug 2023 • Paper 1",
                questionHindi = "लेव वायगोत्स्की के अनुसार, बच्चे के वास्तविक विकास स्तर (जो वह अकेले कर सकता है) और उसके संभावित विकास स्तर (जो वह किसी कुशल वयस्क की सहायता से कर सकता है) के बीच के अंतर को क्या कहा जाता है?",
                questionEnglish = "According to Lev Vygotsky, the zone between what a learner can do without help and what they can do with help is known as:",
                options = listOf(
                    "आत्मसातीकरण (Assimilation)",
                    "समीपस्थ विकास का क्षेत्र (Zone of Proximal Development - ZPD)",
                    "संज्ञानवादी संतुलन (Equilibration)",
                    "आंतरिक भाषा (Inner Speech)"
                ),
                correctOptionIndex = 1,
                explanationHindi = "लेव वायगोत्स्की ने 'ZPD' (Zone of Proximal Development) को वह क्षेत्र परिभाषित किया है जहां बालक किसी अधिक जानकार व्यक्ति (MKO) की सहायता से नई अवधारणाएं और कौशल सफलतापूर्वक सीख पाता है।"
            ),
            Question(
                id = "q_cdp_01_03",
                chapterId = "cdp_ch01",
                subjectCode = "CDP",
                examYearText = "CTET Dec 2022 • Paper 1",
                questionHindi = "लॉरेंस कोहलबर्ग के नैतिक विकास के सिद्धांत में 'अच्छा लड़का - अच्छी लड़की' (Good Boy - Nice Girl) उन्मुखीकरण किस स्तर की विशेषता है?",
                questionEnglish = "In Lawrence Kohlberg's theory of moral development, 'Good boy - Nice girl' orientation is characteristic of:",
                options = listOf(
                    "पूर्व-पारंपरिक स्तर (Pre-Conventional Level)",
                    "पारंपरिक स्तर (Conventional Level)",
                    "उत्तर-पारंपरिक स्तर (Post-Conventional Level)",
                    "सामाजिक संविदा स्तर (Social Contract Level)"
                ),
                correctOptionIndex = 1,
                explanationHindi = "कोहलबर्ग के अनुसार 'पारंपरिक स्तर' (Conventional Level) के चरण 3 में बालक दूसरों की स्वीकृति, प्रशंसा और सामाजिक तालमेल बनाए रखने के लिए नियमों का पालन करता है (अच्छा लड़का/अच्छी लड़की उन्मुखीकरण)।"
            ),
            Question(
                id = "q_cdp_01_04",
                chapterId = "cdp_ch01",
                subjectCode = "CDP",
                examYearText = "CTET Jan 2023 • Paper 2",
                questionHindi = "एक अध्यापिका गणित की कठिन पहेली हल करने में छात्र को संकेत (Hints), आधा हल किया उदाहरण और सही दिशा में प्रश्न पूछकर सहायता देती है। वायगोत्स्की के अनुसार यह तकनीक क्या कहलाती है?",
                questionEnglish = "A teacher provides hints and half-solved examples to guide a student. According to Vygotsky, this technique is called:",
                options = listOf(
                    "पाड़ / मचान (Scaffolding)",
                    "सकारात्मक पुनर्बलन (Positive Reinforcement)",
                    "शास्त्रीय अनुबंधन (Classical Conditioning)",
                    "संज्ञानात्मक द्वंद्व (Cognitive Conflict)"
                ),
                correctOptionIndex = 0,
                explanationHindi = "वायगोत्स्की के सामाजिक-सांस्कृतिक सिद्धांत में सीखने के दौरान वयस्क या शिक्षक द्वारा दी जाने वाली अस्थायी सहायता, संकेत या मार्गदर्शन को 'पाड़' या 'मचान' (Scaffolding) कहा जाता है। बालक के स्वतंत्र होने पर यह सहायता धीरे-धीरे हटा ली जाती है।"
            )
        )

        private val cdpCh02Questions = listOf(
            Question(
                id = "q_cdp_02_01",
                chapterId = "cdp_ch02",
                subjectCode = "CDP",
                examYearText = "CTET Jan 2024 • Paper 1",
                questionHindi = "एक प्राथमिक कक्षा का छात्र पढ़ते समय 'b' को 'd' और 'saw' को 'was' पढ़ता है तथा शब्दों को उल्टा लिखता है। वह किस अधिगम विकार (Learning Disability) से ग्रसित है?",
                questionEnglish = "A child struggles with reading, confusing 'b' with 'd' and 'saw' with 'was'. This condition is termed:",
                options = listOf(
                    "डिस्कैलकुलिया (Dyscalculia - गणितीय विकार)",
                    "डिस्लेक्सिया (Dyslexia - पठन विकार)",
                    "डिस्ग्राफिया (Dysgraphia - लेखन विकार)",
                    "डिस्प्रेक्सिया (Dyspraxia - गति-कौशल विकार)"
                ),
                correctOptionIndex = 1,
                explanationHindi = "'डिस्लेक्सिया' (Dyslexia) एक तंत्रिका संबंधी पठन विकार है जिसमें बालक को वर्णों की पहचान, ध्वन्यात्मक संबंध जोड़ने और शब्दों को सही क्रम में पढ़ने में कठिनाई होती है। वह b/d और saw/was में भ्रमित हो जाता है।"
            ),
            Question(
                id = "q_cdp_02_02",
                chapterId = "cdp_ch02",
                subjectCode = "CDP",
                examYearText = "CTET Aug 2023 • Paper 1",
                questionHindi = "समावेशी शिक्षा (Inclusive Education) के मूल दर्शन के अनुसार निम्नलिखित में से कौन-सा कथन सर्वाधिक उपयुक्त है?",
                questionEnglish = "According to the core philosophy of Inclusive Education, which statement is most appropriate?",
                options = listOf(
                    "विशेष आवश्यकता वाले बच्चों को अलग विशेष विद्यालयों में ही पढ़ाया जाना चाहिए",
                    "व्यवस्था को बच्चों की विविध आवश्यकताओं के अनुसार लचीला और अनुकूलित होना चाहिए",
                    "बच्चों को व्यवस्था के अनुसार खुद को ढालना अनिवार्य होना चाहिए",
                    "केवल प्रतिभाशाली बच्चों पर ध्यान केंद्रित करना चाहिए"
                ),
                correctOptionIndex = 1,
                explanationHindi = "समावेशी शिक्षा का मूल मंत्र है: 'व्यवस्था बच्चे के लिए है, बच्चा व्यवस्था के लिए नहीं'। विद्यालय, पाठ्यक्रम और शिक्षण विधियों को प्रत्येक बालक की वैयक्तिक भिन्नताओं और आवश्यकताओं के अनुसार अनुकूलित (Adapted) किया जाना चाहिए।"
            )
        )

        private val evsCh01Questions = listOf(
            Question(
                id = "q_evs_01_01",
                chapterId = "evs_ch01",
                subjectCode = "EVS",
                examYearText = "CTET Jan 2024 • Paper 1",
                questionHindi = "निम्नलिखित में से उस स्तनधारी जीव को पहचानिए जो भालू जैसा दिखता है, वृक्षों की शाखाओं पर लगभग 17 घंटे उल्टा लटक कर सोता है और जिस पेड़ पर रहता है उसी की पत्तियां खाता है:",
                questionEnglish = "Identify the mammal that looks like a bear, sleeps hanging upside down on tree branches for nearly 17 hours a day, and eats the leaves of that same tree:",
                options = listOf(
                    "चिम्पैंजी (Chimpanzee)",
                    "स्लॉथ (Sloth)",
                    "लंगूर (Langur)",
                    "पांडा (Panda)"
                ),
                correctOptionIndex = 1,
                explanationHindi = "NCERT कक्षा 5 पर्यावरण पाठ 1 के अनुसार, 'स्लॉथ' (Sloth) भालू जैसा दिखने वाला एक सुस्त जीव है। यह दिन में लगभग 17 घंटे पेड़ों की शाखाओं पर उल्टे लटक कर सोता है। इसकी औसत आयु लगभग 40 वर्ष होती है और यह अपने पूरे जीवन में केवल 7-8 पेड़ों पर ही घूम पाता है।"
            ),
            Question(
                id = "q_evs_01_02",
                chapterId = "evs_ch01",
                subjectCode = "EVS",
                examYearText = "CTET Aug 2023 • Paper 1",
                questionHindi = "हमारे देश के किस राज्य में अत्यधिक भारी वर्षा होने के कारण ग्रामीण लोग अपने घर जमीन से लगभग 10 से 12 फीट (3 से 3.5 मीटर) ऊंचे बांस के मजबूत खंभों पर बनाते हैं?",
                questionEnglish = "In which Indian state do people construct houses 10-12 feet above the ground on strong bamboo pillars due to heavy rains?",
                options = listOf(
                    "राजस्थान (Rajasthan)",
                    "लद्दाख (Ladakh)",
                    "असम (Assam)",
                    "उत्तराखंड (Uttarakhand)"
                ),
                correctOptionIndex = 2,
                explanationHindi = "NCERT कक्षा 3 पाठ 'यहाँ से वहाँ' के अनुसार असम में बहुत अधिक वर्षा और बाढ़ की स्थिति रहने के कारण लोग अपने घर जमीन से 10 से 12 फीट ऊंचे बांस के खंभों पर बनाते हैं ताकि बाढ़ का पानी घरों में न घुसे।"
            )
        )

        private val mathCh01Questions = listOf(
            Question(
                id = "q_math_01_01",
                chapterId = "math_ch01",
                subjectCode = "MATH",
                examYearText = "CTET Jan 2024 • Paper 1",
                questionHindi = "संख्या 70560 में 5 के स्थानीय मान (Place Value) और 6 के जातीय मान (Face Value) का गुणनफल क्या होगा?",
                questionEnglish = "What is the product of the place value of 5 and the face value of 6 in the number 70560?",
                options = listOf(
                    "3000",
                    "30000",
                    "300",
                    "500"
                ),
                correctOptionIndex = 0,
                explanationHindi = "संख्या 70560 में:\n• 5 का स्थानीय मान = 5 × 100 = 500\n• 6 का जातीय मान = 6\nगुणनफल = 500 × 6 = 3000।"
            ),
            Question(
                id = "q_math_01_02",
                chapterId = "math_ch01",
                subjectCode = "MATH",
                examYearText = "CTET Dec 2022 • Paper 1",
                questionHindi = "प्राथमिक स्तर पर गणित की पाठ्यपुस्तक में दी गई पहेलियों (Puzzles) का प्रमुख उद्देश्य क्या है?",
                questionEnglish = "What is the primary objective of riddles and puzzles in primary mathematics textbooks?",
                options = listOf(
                    "छात्रों को व्यस्त रखकर कक्षा में अनुशासन बनाए रखना",
                    "छात्रों में तार्किक एवं समस्या समाधान कौशल का विकास करना",
                    "कठिन प्रश्नों द्वारा छात्रों का परीक्षण करना",
                    "पाठ्यक्रम को जल्दी पूरा करना"
                ),
                correctOptionIndex = 1,
                explanationHindi = "NCF 2005 के अनुसार प्राथमिक गणित में पहेलियां और खेल छात्रों में गणितीय अभिरुचि, अमूर्त चिंतन और समस्या समाधान (Problem Solving) के तार्किक कौशलों को प्रोत्साहित करते हैं।"
            )
        )

        private val hindiCh01Questions = listOf(
            Question(
                id = "q_hindi_01_01",
                chapterId = "hindi_ch01",
                subjectCode = "LANG-1",
                examYearText = "CTET Jan 2024 • Paper 1",
                questionHindi = "नोम चॉम्स्की (Noam Chomsky) के अनुसार, प्रत्येक बालक जन्मजात किस क्षमता के साथ जन्म लेता है जिसके कारण वह भाषा को सरलता से सीख पाता है?",
                questionEnglish = "According to Noam Chomsky, every child is born with an innate capacity to acquire language known as:",
                options = listOf(
                    "भाषा अर्जन यंत्र (LAD - Language Acquisition Device)",
                    "अनुकूलित अनुक्रिया (Conditioned Response)",
                    "अवलोकन आधारित अधिगम (Observational Learning)",
                    "शाब्दिक संघटन क्षमता"
                ),
                correctOptionIndex = 0,
                explanationHindi = "नोम चॉम्स्की का मानना है कि मानव शिशुओं में भाषा सीखने की जन्मजात क्षमता होती है। उनके मस्तिष्क में एक 'भाषा अर्जन यंत्र' (LAD - Language Acquisition Device) विद्यमान होता है जो सार्वभौमिक व्याकरण के नियमों को स्वतः संसाधित करता है।"
            )
        )

        private fun defaultChapterQuestions(chId: String) = listOf(
            Question(
                id = "${chId}_q01",
                chapterId = chId,
                subjectCode = "GEN",
                examYearText = "CTET Official PYQ",
                questionHindi = "राष्ट्रीय शिक्षा नीति (NEP 2020) के अनुसार प्राथमिक स्तर पर शिक्षण का माध्यम क्या होना चाहिए?",
                questionEnglish = "According to NEP 2020, what should be the medium of instruction at primary level?",
                options = listOf(
                    "केवल अंग्रेजी भाषा",
                    "मातृभाषा / स्थानीय घरेलू भाषा",
                    "केवल राज्य की राजभाषा",
                    "शास्त्रीय भाषा"
                ),
                correctOptionIndex = 1,
                explanationHindi = "NEP 2020 के पैराग्राफ 4.11 के अनुसार, कम से कम ग्रेड 5 (प्राथमिक स्तर) तक शिक्षण का माध्यम बच्चे की घरेलू भाषा, मातृभाषा या स्थानीय भाषा में होना चाहिए, जिससे अवधारणाएं स्पष्ट हों।"
            )
        )
    }
}
