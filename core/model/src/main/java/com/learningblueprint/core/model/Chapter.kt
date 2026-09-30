package com.learningblueprint.core.model

import org.json.JSONArray
import org.json.JSONObject

data class Chapter(
    val id: String,
    val subjectCode: String,
    val chapterNumber: Int,
    val titleHindi: String,
    val titleEnglish: String,
    val importanceBadge: String,
    val isHighYield: Boolean = false,
    val pyqCount: Int,
    val keyConcepts: List<String>,
    val weightageText: String
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("subjectCode", subjectCode)
        put("chapterNumber", chapterNumber)
        put("titleHindi", titleHindi)
        put("titleEnglish", titleEnglish)
        put("importanceBadge", importanceBadge)
        put("isHighYield", isHighYield)
        put("pyqCount", pyqCount)
        val arr = JSONArray()
        keyConcepts.forEach { arr.put(it) }
        put("keyConcepts", arr)
        put("weightageText", weightageText)
    }

    companion object {
        fun fromJson(obj: JSONObject): Chapter {
            val concepts = mutableListOf<String>()
            val arr = obj.optJSONArray("keyConcepts")
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    concepts.add(arr.getString(i))
                }
            }
            return Chapter(
                id = obj.getString("id"),
                subjectCode = obj.getString("subjectCode"),
                chapterNumber = obj.optInt("chapterNumber", 1),
                titleHindi = obj.getString("titleHindi"),
                titleEnglish = obj.optString("titleEnglish", ""),
                importanceBadge = obj.optString("importanceBadge", "महत्वपूर्ण"),
                isHighYield = obj.optBoolean("isHighYield", false),
                pyqCount = obj.optInt("pyqCount", 20),
                keyConcepts = concepts,
                weightageText = obj.optString("weightageText", "")
            )
        }

        fun getChaptersForSubject(subjectCode: String): List<Chapter> {
            val normalized = subjectCode.uppercase()
            return when {
                normalized.contains("CDP") -> cdpChapters
                normalized.contains("EVS") -> evsChapters
                normalized.contains("MATH") -> mathChapters
                normalized.contains("LANG-1") || normalized.contains("HINDI") -> hindiChapters
                normalized.contains("LANG-2") -> lang2Chapters
                normalized.contains("SCI") -> scienceChapters
                normalized.contains("SST") -> sstChapters
                else -> defaultChapters(subjectCode)
            }
        }

        private val cdpChapters = listOf(
            Chapter(
                id = "cdp_ch01",
                subjectCode = "CDP",
                chapterNumber = 1,
                titleHindi = "पियाजे, कोहलबर्ग एवं वायगोत्स्की के सिद्धांत",
                titleEnglish = "Piaget, Kohlberg & Vygotsky Theories",
                importanceBadge = "★ सर्वाधिक महत्वपूर्ण • 4-5 प्रश्न",
                isHighYield = true,
                pyqCount = 78,
                keyConcepts = listOf(
                    "जीन पियाजे: संज्ञानात्मक विकास की 4 अवस्थाएं (संवेदी-गामक, पूर्व-संक्रियात्मक, मूर्त, औपचारिक)",
                    "आत्मसातीकरण (Assimilation), समायोजन (Accommodation) एवं स्कीमा (Schema)",
                    "लेव वायगोत्स्की: सामाजिक-सांस्कृतिक सिद्धांत, ZPD (समीपस्थ विकास क्षेत्र), मचान/पाड़ (Scaffolding)",
                    "लॉरेंस कोहलबर्ग: नैतिक विकास की 3 स्तर व 6 अवस्थाएं (हिंज दुविधा)"
                ),
                weightageText = "प्रत्येक CTET/TET प्रश्नपत्र में 4 से 6 प्रश्न अनिवार्य रूप से पूछे जाते हैं"
            ),
            Chapter(
                id = "cdp_ch02",
                subjectCode = "CDP",
                chapterNumber = 2,
                titleHindi = "समावेशी शिक्षा एवं विशेष आवश्यकता वाले बालक",
                titleEnglish = "Inclusive Education & Diverse Learners",
                importanceBadge = "★ अनिवार्य खंड • 5 प्रश्न फिक्स",
                isHighYield = true,
                pyqCount = 64,
                keyConcepts = listOf(
                    "समावेशी शिक्षा की अवधारणा: बिना भेदभाव सभी बच्चों को सामान्य कक्षा में शिक्षा",
                    "अधिगम अक्षमताएं: डिस्लेक्सिया (पठन विकार), डिस्ग्राफिया (लेखन विकार), डिस्कैलकुलिया (गणितीय विकार)",
                    "प्रतिभाशाली, सृजनात्मक एवं वंचित वर्ग के बालकों की पहचान व शिक्षण रणनीतियां",
                    "RPwD Act 2016 एवं NEP 2020 में समावेशी शिक्षा के विशेष प्रावधान"
                ),
                weightageText = "पाठ्यक्रम के अनुसार न्यूनतम 5 प्रश्न इस एकल विषय से निर्धारित हैं"
            ),
            Chapter(
                id = "cdp_ch03",
                subjectCode = "CDP",
                chapterNumber = 3,
                titleHindi = "बाल विकास की अवधारणा एवं अधिगम से संबंध",
                titleEnglish = "Concept of Development & Learning Link",
                importanceBadge = "महत्वपूर्ण आधार • 2-3 प्रश्न",
                isHighYield = false,
                pyqCount = 42,
                keyConcepts = listOf(
                    "वृद्धि (मात्रात्मक) बनाम विकास (गुणात्मक एवं सतत प्रक्रिया)",
                    "विकास के सिद्धांत: शीर्षाभिमुख (Cephalocaudal) व समीप-दूराभिमुख (Proximodistal) दिशा",
                    "विकास के आयाम: शारीरिक, संज्ञानात्मक, सामाजिक, संवेगात्मक व नैतिक",
                    "आनुवंशिकता (प्रकृति) और पर्यावरण (पोषण) की अंतःक्रिया"
                ),
                weightageText = "विगत 10 वर्षों में लगभग 42 बार प्रत्यक्ष प्रश्न पूछे गए हैं"
            ),
            Chapter(
                id = "cdp_ch04",
                subjectCode = "CDP",
                chapterNumber = 4,
                titleHindi = "बाल-केंद्रित एवं प्रगतिशील शिक्षा की अवधारणा",
                titleEnglish = "Child-Centered & Progressive Education",
                importanceBadge = "पेडागॉजी कोर • 2-3 प्रश्न",
                isHighYield = true,
                pyqCount = 38,
                keyConcepts = listOf(
                    "जॉन ड्यूवी: 'करके सीखना' (Learning by Doing) एवं समस्या समाधान विधि",
                    "शिक्षक की भूमिका: मात्र ज्ञानदाता नहीं, बल्कि 'सुविधादाता/सुगमकर्ता' (Facilitator)",
                    "रटने की प्रणाली (Rote Learning) का निषेध, गतिविधि आधारित शिक्षण",
                    "सक्रिय अधिगमकर्ता के रूप में बालक की जिज्ञासा का सम्मान"
                ),
                weightageText = "NCF-2005 और NEP-2020 आधारित सभी पेडागॉजिकल प्रश्नों की कुंजी"
            ),
            Chapter(
                id = "cdp_ch05",
                subjectCode = "CDP",
                chapterNumber = 5,
                titleHindi = "बुद्धि निर्माण एवं हावर्ड गार्डनर का बहुबुद्धि सिद्धांत",
                titleEnglish = "Intelligence & Howard Gardner's Multiple Intelligences",
                importanceBadge = "★ हॉट टॉपिक • 1-2 प्रश्न",
                isHighYield = true,
                pyqCount = 46,
                keyConcepts = listOf(
                    "हावर्ड गार्डनर की 8 प्रकार की बुद्धिमत्ता (भाषाई, तार्किक-गणितीय, स्थानिक, शारीरिक-गतिक, आदि)",
                    "अंतःवैयक्तिक (Intrapersonal) बनाम अंतर-वैयक्तिक (Interpersonal) बुद्धि में अंतर",
                    "स्पीयरमैन का द्विकारक सिद्धांत (G-Factor व S-Factor)",
                    "बुद्धि लब्धि (IQ) की सीमाएं एवं संवेगात्मक बुद्धि (EQ) का महत्व"
                ),
                weightageText = "गार्डनर के बहुबुद्धि सिद्धांत से प्रतिवर्ष 1 से 2 प्रश्न निश्चित रहते हैं"
            ),
            Chapter(
                id = "cdp_ch06",
                subjectCode = "CDP",
                chapterNumber = 6,
                titleHindi = "अधिगम का आकलन बनाम अधिगम के लिए आकलन (CCE)",
                titleEnglish = "Assessment OF Learning vs FOR Learning (CCE)",
                importanceBadge = "मूल्यांकन कोर • 2-3 प्रश्न",
                isHighYield = false,
                pyqCount = 39,
                keyConcepts = listOf(
                    "रचनात्मक आकलन (Formative - सीखने के लिए) बनाम योगात्मक आकलन (Summative - सीखने का)",
                    "सीखने के रूप में आकलन (Assessment as Learning) - स्व-मूल्यांकन",
                    "सतत एवं व्यापक मूल्यांकन (CCE): पोर्टफोलियो, उपाख्यानात्मक अभिलेख (Anecdotal Record), रूब्रिक्स",
                    "डायग्नोस्टिक टेस्ट (निदानात्मक परीक्षण) एवं उपचारात्मक शिक्षण (Remedial Teaching)"
                ),
                weightageText = "शिक्षण अधिगम प्रक्रिया में बच्चे की प्रगति ट्रैक करने से संबंधित प्रश्न"
            )
        )

        private val evsChapters = listOf(
            Chapter(
                id = "evs_ch01",
                subjectCode = "EVS",
                chapterNumber = 1,
                titleHindi = "NCERT पर्यावरण थीम्स: परिवार, मित्र, भोजन व आश्रय",
                titleEnglish = "NCERT EVS Themes: Family, Food & Shelter",
                importanceBadge = "★ NCERT सार • 4-5 प्रश्न",
                isHighYield = true,
                pyqCount = 72,
                keyConcepts = listOf(
                    "EVS की 6 मुख्य थीम्स: परिवार व मित्र, भोजन, पानी, आवास, यात्रा, चीजें जो हम बनाते हैं",
                    "विभिन्न क्षेत्रों के विशेष घर: असम (बांस के खंभे), मनाली (पत्थर-लकड़ी), लद्दाख (दो मंजिला मिट्टी घर), राजस्थान (मिट्टी व कंटीली झाड़ियां)",
                    "भोजन संस्कृति: टैपियोका (केरल), लिंग-हू-फेन (हांगकांग का सांप सूप), सरसों के तेल में मछली (कश्मीर)",
                    "हाथी, मधुमक्खी (अनीता की मधुमक्खियां), स्लॉथ (17 घंटे उल्टा लटकने वाला भालू समान जीव)"
                ),
                weightageText = "कक्षा 3 से 5 की NCERT पाठ्यपुस्तकों से सीधे पूछे जाने वाले तथ्य"
            ),
            Chapter(
                id = "evs_ch02",
                subjectCode = "EVS",
                chapterNumber = 2,
                titleHindi = "मानचित्र अध्ययन, दिशा एवं सापेक्ष दूरी",
                titleEnglish = "Map Reading, Directions & Spatial Sense",
                importanceBadge = "★ व्यावहारिक प्रश्न • 2-3 प्रश्न",
                isHighYield = true,
                pyqCount = 52,
                keyConcepts = listOf(
                    "भारत के नक्शे पर राज्यों की सापेक्ष स्थिति (जैसे: दिल्ली के सापेक्ष मध्य प्रदेश व बिहार की दिशा)",
                    "अरब सागर और बंगाल की खाड़ी की सीमा से लगे भारतीय तटीय राज्य",
                    "चाल, दूरी एवं समय आधारित ट्रेन यात्रा की समय-सारणी वाले संख्यात्मक प्रश्न",
                    "मानचित्र पैमाना (Scale: 1 सेमी = 110 मीटर) पर दूरी की गणना"
                ),
                weightageText = "प्रत्येक परीक्षा में 1 दिशा/मानचित्र और 1 ट्रेन/गति वाला प्रश्न अनिवार्य"
            ),
            Chapter(
                id = "evs_ch03",
                subjectCode = "EVS",
                chapterNumber = 3,
                titleHindi = "पर्यावरण अध्ययन शिक्षणशास्त्र (EVS Pedagogy)",
                titleEnglish = "EVS Pedagogical Issues & Integration",
                importanceBadge = "★ 15 अंक पेडागॉजी • 15 प्रश्न फिक्स",
                isHighYield = true,
                pyqCount = 68,
                keyConcepts = listOf(
                    "पर्यावरण अध्ययन का एकीकृत स्वरूप (विज्ञान, सामाजिक विज्ञान व पर्यावरण का समावेशन)",
                    "कक्षा 1 व 2 में EVS की अलग पुस्तक नहीं - भाषा व गणित के माध्यम से अध्ययन",
                    "पर्यावरणीय भ्रमण, सर्वेक्षण, स्थानीय संसाधनों व प्रत्यक्ष अनुभवों का महत्व",
                    "पर्यावरण संरक्षण के प्रति संवेदनशीलता एवं जेंडर रूढ़िवादिता का खंडन"
                ),
                weightageText = "EVS के कुल 30 अंकों में से सीधे 15 प्रश्न पेडागॉजी से आते हैं"
            ),
            Chapter(
                id = "evs_ch04",
                subjectCode = "EVS",
                chapterNumber = 4,
                titleHindi = "जल, यात्रा एवं पर्यावरण संरक्षण के नायक",
                titleEnglish = "Water, Travel & Conservation Heroes",
                importanceBadge = "महत्वपूर्ण NCERT • 2-3 प्रश्न",
                isHighYield = false,
                pyqCount = 35,
                keyConcepts = listOf(
                    "अल-बिरूनी का भारत के तालाबों का ऐतिहासिक वर्णन (उज्बेकिस्तान का यात्री)",
                    "घड़सीसर झील (जैसलमेर, राजस्थान) और वर्षा जल संचयन (बावड़ी/Stepwell)",
                    "खेजड़ी के वृक्ष (राजस्थान का अमृत देवी विश्नोई बलिदान), चिपको आंदोलन",
                    "कर्णम मल्लेश्वरी (वेटलिफ्टर), बछेंद्री पाल (एवरेस्ट विजेता), सूर्यमणि (कुडुख भाषा - तोरांग केंद्र)"
                ),
                weightageText = "NCERT पाठों के प्रेरणादायी व्यक्तित्वों पर आधारित प्रश्न"
            )
        )

        private val mathChapters = listOf(
            Chapter(
                id = "math_ch01",
                subjectCode = "MATH",
                chapterNumber = 1,
                titleHindi = "संख्या पद्धति, स्थानीय मान व मूलभूत संक्रियाएं",
                titleEnglish = "Number System, Place Value & Operations",
                importanceBadge = "★ आधारभूत अंकगणित • 3-4 प्रश्न",
                isHighYield = true,
                pyqCount = 58,
                keyConcepts = listOf(
                    "स्थानीय मान (Place Value) और जातीय मान (Face Value) में अंतर व योग/अंतर",
                    "दशमलव संख्याएं, भिन्नों का आरोही/अवरोही क्रम, और संक्रियाएं",
                    "अभाज्य संख्याएं, गुणनखण्ड (Factors) एवं गुणज (Multiples - LCM/HCF)",
                    "दैनिक जीवन से जुड़ी जोड़-घटाना-गुणा-भाग की शाब्दिक समस्याएं"
                ),
                weightageText = "प्राथमिक गणित का सबसे स्कोरिंग और सहज भाग"
            ),
            Chapter(
                id = "math_ch02",
                subjectCode = "MATH",
                chapterNumber = 2,
                titleHindi = "वैन हील का ज्यामितीय चिंतन स्तर एवं आकृतियां",
                titleEnglish = "Van Hiele Geometric Thinking & 2D/3D Shapes",
                importanceBadge = "★ पेडागॉजी का अनिवार्य प्रश्न • 2 प्रश्न",
                isHighYield = true,
                pyqCount = 48,
                keyConcepts = listOf(
                    "वैन हील के स्तर: स्तर 0 (प्रत्यक्षीकरण), स्तर 1 (विश्लेषण), स्तर 2 (अनौपचारिक निगमन), स्तर 3 (औपचारिक), स्तर 4 (दृढ़ता)",
                    "2D और 3D आकृतियों के गुण: वर्ग, आयत, त्रिभुज, घन, घनाब के कोने, किनारे व फलक",
                    "सममिति (Symmetry) की रेखाएं और घूर्णन सममिति",
                    "टैंग्राम (Tangram), जियोबोर्ड (Geoboard) और ग्रिड पेपर द्वारा ज्यामिति शिक्षण"
                ),
                weightageText = "वैन हील मॉडल से हर बार न्यूनतम 1 से 2 प्रश्न पूछे जाते हैं"
            ),
            Chapter(
                id = "math_ch03",
                subjectCode = "MATH",
                chapterNumber = 3,
                titleHindi = "गणित शिक्षणशास्त्र एवं त्रुटि विश्लेषण",
                titleEnglish = "Mathematics Pedagogy & Error Analysis",
                importanceBadge = "★ 15 अंक पेडागॉजी • 15 प्रश्न फिक्स",
                isHighYield = true,
                pyqCount = 62,
                keyConcepts = listOf(
                    "गणित की प्रकृति: तार्किक, सटीक, पदानुक्रमित (Hierarchical) एवं अमूर्त",
                    "गणितीय चिंतन का विकास: प्राथमिक स्तर पर मूर्त से अमूर्त (Concrete to Abstract) उपागम",
                    "छात्रों की त्रुटियां (Errors): अधिगम प्रक्रिया का आवश्यक अंग व खिड़की",
                    "गणित का भय (Math Anxiety) दूर करने के उपाय एवं गणितीय पहेलियों की भूमिका"
                ),
                weightageText = "NCF अनुसार प्राथमिक स्तर पर गणितीयकरण (Mathematization) का लक्ष्य"
            ),
            Chapter(
                id = "math_ch04",
                subjectCode = "MATH",
                chapterNumber = 4,
                titleHindi = "मापन, भार, समय, धारिता एवं पैटर्न",
                titleEnglish = "Measurement, Time, Volume & Patterns",
                importanceBadge = "व्यावहारिक गणित • 2-3 प्रश्न",
                isHighYield = false,
                pyqCount = 40,
                keyConcepts = listOf(
                    "इकाइयों का रूपांतरण: किलोग्राम-ग्राम, लीटर-मिलीलीटर, मीटर-सेंटीमीटर-मिलीमीटर",
                    "घड़ी का समय (12-घंटे व 24-घंटे रेलवे समय) और समयावधि की गणना",
                    "परिमाप एवं क्षेत्रफल (आयत व वर्ग का दैनिक जीवन में अनुप्रयोग)",
                    "संख्या एवं ज्यामितीय पैटर्न की पहचान व अगली संख्या ज्ञात करना"
                ),
                weightageText = "सीधे व्यावहारिक गणनाओं पर आधारित स्कोरिंग प्रश्न"
            )
        )

        private val hindiChapters = listOf(
            Chapter(
                id = "hindi_ch01",
                subjectCode = "LANG-1",
                chapterNumber = 1,
                titleHindi = "भाषा अर्जन बनाम भाषा अधिगम (चॉम्स्की का सिद्धांत)",
                titleEnglish = "Language Acquisition vs Learning (Chomsky)",
                importanceBadge = "★ पेडागॉजी कोर • 2-3 प्रश्न",
                isHighYield = true,
                pyqCount = 55,
                keyConcepts = listOf(
                    "भाषा अर्जन (स्वाभाविक/मातृभाषा) बनाम भाषा अधिगम (प्रयासपूर्ण/औपचारिक/द्वितीय भाषा)",
                    "नोम चॉम्स्की: भाषा अर्जन यंत्र (LAD - Language Acquisition Device) व सार्वभौमिक व्याकरण",
                    "बी.एफ. स्किनर (अनुकरण व पुनर्बलन) बनाम चॉम्स्की का सहजतावादी सिद्धांत",
                    "बहुभाषिकता (Multilingualism): कक्षा में समस्या नहीं बल्कि अमूल्य संसाधन"
                ),
                weightageText = "हिंदी पेडागॉजी में चॉम्स्की एवं अर्जन-अधिगम से 3 प्रश्न तय होते हैं"
            ),
            Chapter(
                id = "hindi_ch02",
                subjectCode = "LANG-1",
                chapterNumber = 2,
                titleHindi = "भाषाई कौशल: सुनना, बोलना, पढ़ना, लिखना (LSRW)",
                titleEnglish = "Four Language Skills (LSRW Integration)",
                importanceBadge = "★ अनिवार्य खंड • 3-4 प्रश्न",
                isHighYield = true,
                pyqCount = 60,
                keyConcepts = listOf(
                    "कौशलों का एकीकृत अंतःसंबंध (चारों कौशल एक साथ सीखे जाते हैं, रेखीय रूप से नहीं)",
                    "पठन कौशल: केवल शब्दों का उच्चारण नहीं बल्कि 'अर्थ ग्रहण करना' मुख्य उद्देश्य",
                    "लेखन कौशल: विचारों की मौलिक व स्वतंत्र अभिव्यक्ति (सुलेख मात्र नहीं)",
                    "मौन पठन (गहन अध्ययन) बनाम सस्वर पठन (उच्चारण व प्रवाह)"
                ),
                weightageText = "प्राथमिक स्तर पर भाषाई दक्षताओं के विकास पर केंद्रित प्रश्न"
            ),
            Chapter(
                id = "hindi_ch03",
                subjectCode = "LANG-1",
                chapterNumber = 3,
                titleHindi = "अपठित गद्यांश एवं पद्यांश हल करने की तकनीक",
                titleEnglish = "Unseen Passage & Poem Comprehension",
                importanceBadge = "★ 15 अंक फिक्स • 15 प्रश्न",
                isHighYield = true,
                pyqCount = 70,
                keyConcepts = listOf(
                    "गद्यांश (9 प्रश्न) व पद्यांश (6 प्रश्न): पाठ्य-आधारित समझ व निष्कर्ष निकालना",
                    "संदर्भ में व्याकरण: संधि, समास, प्रत्यय, उपसर्ग, विलोम व पर्यायवाची शब्द",
                    "व्याकरण का अलग से कोई प्रश्न नहीं - केवल गद्यांश/पद्यांश के संदर्भ में ही पूछा जाता है",
                    "कविता का मुख्य भाव एवं रसानुभूति आधारित प्रश्न"
                ),
                weightageText = "हिंदी के 30 अंकों में से पूरे 15 अंक सीधे पैसेज से आते हैं"
            )
        )

        private val lang2Chapters = listOf(
            Chapter(
                id = "lang2_ch01",
                subjectCode = "LANG-2",
                chapterNumber = 1,
                titleHindi = "द्वितीय भाषा शिक्षण सिद्धांत एवं विधियां",
                titleEnglish = "Second Language Pedagogy & Methods",
                importanceBadge = "★ पेडागॉजी कोर • 4-5 प्रश्न",
                isHighYield = true,
                pyqCount = 45,
                keyConcepts = listOf(
                    "स्टीफन क्रैशन का प्राकृतिक उपागम (Natural Approach) एवं इनपुट परिकल्पना (i+1)",
                    "व्याकरण अनुवाद विधि (Grammar Translation) बनाम प्रत्यक्ष विधि (Direct Method)",
                    "संप्रेषणात्मक भाषा शिक्षण (CLT - Communicative Language Teaching)",
                    "भाषा त्रुटियां एवं फीडबैक रणनीति"
                ),
                weightageText = "अंग्रेजी/संस्कृत पेडागॉजी के 15 अंकों का मुख्य आधार"
            ),
            Chapter(
                id = "lang2_ch02",
                subjectCode = "LANG-2",
                chapterNumber = 2,
                titleHindi = "अपठित गद्य एवं पद्य बोध (Language II)",
                titleEnglish = "Comprehension Passages (Prose & Poetry)",
                importanceBadge = "★ 15 अंक फिक्स • 15 प्रश्न",
                isHighYield = true,
                pyqCount = 65,
                keyConcepts = listOf(
                    "दो अपठित गद्यांश (अथवा 1 गद्य + 1 पद्य) पर आधारित बौद्धिक प्रश्न",
                    "संदर्भगत व्याकरण: Parts of Speech, Tenses, प्रत्यय-उपसर्ग",
                    "शीर्षक चयन एवं गद्यांश का केंद्रीय भाव",
                    "समय प्रबंधन द्वारा सटीक उत्तर खोजना"
                ),
                weightageText = "द्वितीय भाषा के 15 अंक सीधे गद्यांशों की समझ पर निर्भर हैं"
            )
        )

        private val scienceChapters = listOf(
            Chapter(
                id = "sci_ch01",
                subjectCode = "SCI-MATH",
                chapterNumber = 1,
                titleHindi = "भोजन, पोषण एवं सजीव जगत (NCERT 6-8)",
                titleEnglish = "Food, Nutrition & Living World",
                importanceBadge = "★ उच्च प्राथमिक कोर • 5-6 प्रश्न",
                isHighYield = true,
                pyqCount = 42,
                keyConcepts = listOf(
                    "पादपों एवं जंतुओं में पोषण: प्रकाश संश्लेषण, पाचन तंत्र, श्वसन व परिसंचरण",
                    "कोशिका संरचना एवं कार्य (पादप बनाम जंतु कोशिका)",
                    "सूक्ष्मजीव: मित्र एवं शत्रु (जीवाणु, विषाणु, कवक व किण्वन)",
                    "पौधों में जनन व अनुकूलन"
                ),
                weightageText = "कक्षा 6 से 8 NCERT विज्ञान जीवविज्ञान खंड का मुख्य हिस्सा"
            ),
            Chapter(
                id = "sci_ch02",
                subjectCode = "SCI-MATH",
                chapterNumber = 2,
                titleHindi = "पदार्थ, बल, गति, प्रकाश एवं विद्युत चुंबकत्व",
                titleEnglish = "Materials, Force, Motion, Light & Electricity",
                importanceBadge = "★ भौतिक-रसायन खंड • 6-7 प्रश्न",
                isHighYield = true,
                pyqCount = 50,
                keyConcepts = listOf(
                    "भौतिक एवं रासायनिक परिवर्तन, अम्ल-क्षार व लवण (लिटमस, फिनॉल्फथेलिन सूचक)",
                    "गति के प्रकार, चाल, घर्षण बल एवं दाब",
                    "प्रकाश: परावर्तन, गोलीय दर्पण व लेंस द्वारा प्रतिबिम्ब निर्माण",
                    "विद्युत परिपथ, हीटिंग प्रभाव व चुंबक के ध्रुव"
                ),
                weightageText = "प्रायोगिक एवं सिद्धांत आधारित भौतिक-रासायनिक अवधारणाएं"
            )
        )

        private val sstChapters = listOf(
            Chapter(
                id = "sst_ch01",
                subjectCode = "SST",
                chapterNumber = 1,
                titleHindi = "इतिहास: प्राचीन, मध्यकालीन एवं आधुनिक भारत",
                titleEnglish = "History: Ancient, Medieval & Modern India",
                importanceBadge = "★ इतिहास खंड • 12-14 प्रश्न",
                isHighYield = true,
                pyqCount = 65,
                keyConcepts = listOf(
                    "आरंभिक मानव, हड़प्पा सभ्यता के प्रमुख नगर व शिल्पकला",
                    "मौर्य एवं गुप्त साम्राज्य: अशोक के अभिलेख, प्रशासन व स्तूप",
                    "दिल्ली सल्तनत एवं मुगल साम्राज्य: भू-राजस्व व वास्तुकला",
                    "1857 का विद्रोह, राष्ट्रीय आंदोलन एवं समाज सुधारक"
                ),
                weightageText = "NCERT 'हमारे अतीत' (भाग 1, 2, 3) का संपूर्ण प्रामाणिक निचोड़"
            ),
            Chapter(
                id = "sst_ch02",
                subjectCode = "SST",
                chapterNumber = 2,
                titleHindi = "भूगोल: सौरमंडल, पृथ्वी, वायुमंडल व पर्यावरण",
                titleEnglish = "Geography: Solar System, Earth & Atmosphere",
                importanceBadge = "★ भूगोल खंड • 10-12 प्रश्न",
                isHighYield = true,
                pyqCount = 55,
                keyConcepts = listOf(
                    "अक्षांश (Latitude) व देशांतर (Longitude), मानक समय (IST) की गणना",
                    "पृथ्वी की गतियां: घूर्णन (दिन-रात) व परिक्रमण (ऋतु परिवर्तन)",
                    "वायुमंडल की परतें (क्षोभमंडल, समतापमंडल) व वायुदाब",
                    "भारत के भौतिक स्वरूप: हिमालय, प्रायद्वीपीय पठार, नदियां व प्राकृतिक वनस्पति"
                ),
                weightageText = "NCERT भूगोल कक्षा 6-8 आधारित उच्च वेटेज अध्याय"
            )
        )

        private fun defaultChapters(code: String) = listOf(
            Chapter(
                id = "${code.lowercase()}_ch01",
                subjectCode = code,
                chapterNumber = 1,
                titleHindi = "विषय की आधारभूत अवधारणाएं एवं सिद्धांत",
                titleEnglish = "Fundamental Concepts & Principles",
                importanceBadge = "महत्वपूर्ण आधार",
                isHighYield = true,
                pyqCount = 25,
                keyConcepts = listOf(
                    "आधिकारिक पाठ्यक्रम के अनुसार कोर टॉपिक्स का अध्ययन",
                    "विगत वर्षों के प्रश्नपत्रों पर आधारित महत्वपूर्ण प्रश्नोत्तर",
                    "शिक्षण विधियों एवं पेडागॉजिकल अनुप्रयोगों का विश्लेषण"
                ),
                weightageText = "विगत वर्षों में नियमित रूप से पूछे जाने वाले प्रश्न"
            )
        )
    }
}
