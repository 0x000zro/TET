package com.example.data.local.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.datasource.DefaultLocalEducationalDataSource
import com.example.data.repository.EducationalRepositoryImpl
import com.example.domain.model.Exam
import com.example.domain.model.Paper
import com.example.domain.model.Question
import com.example.domain.model.QuestionDifficulty
import com.example.domain.model.QuestionOption
import com.example.domain.model.QuestionType
import com.example.domain.model.Subject
import com.example.domain.model.Subtopic
import com.example.domain.model.SyllabusMetadata
import com.example.domain.model.SyllabusNodeType
import com.example.domain.model.Topic
import com.example.domain.repository.EducationalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class SyllabusNavigationIntegrationTest {

    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var repository: EducationalRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        AppDatabase.setTestInstance(database)
        repository = EducationalRepositoryImpl(
            localDataSource = DefaultLocalEducationalDataSource { database },
            ioDispatcher = Dispatchers.Unconfined
        )
    }

    @After
    fun tearDown() {
        AppDatabase.setTestInstance(null)
        database.close()
    }

    @Test
    fun activeExams_appearInDeterministicSortOrder_inactiveExamsHidden() = runBlocking {
        val exam1 = Exam("exam_01", "CTET", "CTET", "Central TET", isActive = true, sortOrder = 2)
        val exam2 = Exam("exam_02", "UPTET", "UPTET", "UP TET", isActive = true, sortOrder = 1)
        val examInactive = Exam("exam_inactive", "Hidden Exam", "HIDDEN", "", isActive = false, sortOrder = 0)

        repository.saveExams(listOf(exam1, exam2, examInactive))

        val activeExams = repository.observeActiveExams().first()
            .sortedWith(compareBy({ it.sortOrder }, { it.name }))

        assertEquals(2, activeExams.size)
        // Deterministic sorting: sortOrder 1 before sortOrder 2
        assertEquals("exam_02", activeExams[0].id)
        assertEquals("exam_01", activeExams[1].id)
        assertFalse(activeExams.any { it.id == "exam_inactive" })
    }

    @Test
    fun papers_filteredBySelectedExam_andInactiveHidden() = runBlocking {
        val exam1 = Exam("exam_01", "CTET", "CTET", "", true, 1)
        val exam2 = Exam("exam_02", "UPTET", "UPTET", "", true, 2)
        repository.saveExams(listOf(exam1, exam2))

        val paper1 = Paper("p1", "exam_01", "Paper 1", "P1", "", isActive = true, sortOrder = 2)
        val paper2 = Paper("p2", "exam_01", "Paper 2", "P2", "", isActive = true, sortOrder = 1)
        val paperInactive = Paper("p_inact", "exam_01", "Paper 3", "P3", "", isActive = false, sortOrder = 3)
        val paperOtherExam = Paper("p_other", "exam_02", "Other Paper", "OP", "", isActive = true, sortOrder = 1)
        repository.savePapers(listOf(paper1, paper2, paperInactive, paperOtherExam))

        val ctetPapers = repository.observeActivePapersByExamId("exam_01").first()
            .sortedWith(compareBy({ it.sortOrder }, { it.name }))

        assertEquals(2, ctetPapers.size)
        assertEquals("p2", ctetPapers[0].id)
        assertEquals("p1", ctetPapers[1].id)
        assertFalse(ctetPapers.any { it.id == "p_inact" })
        assertFalse(ctetPapers.any { it.id == "p_other" })
    }

    @Test
    fun subjects_filteredBySelectedPaper_andDeterministicOrder() = runBlocking {
        val exam = Exam("exam_01", "CTET", "CTET", "", true, 1)
        val paper1 = Paper("paper_01", "exam_01", "Paper 1", "P1", "", true, 1)
        val paper2 = Paper("paper_02", "exam_01", "Paper 2", "P2", "", true, 2)
        repository.saveExam(exam)
        repository.savePapers(listOf(paper1, paper2))

        val sub1 = Subject("sub_01", "paper_01", "Child Development", "CDP", "", isActive = true, sortOrder = 2)
        val sub2 = Subject("sub_02", "paper_01", "Mathematics", "MATH", "", isActive = true, sortOrder = 1)
        val subInactive = Subject("sub_inact", "paper_01", "Old Subject", "", "", isActive = false, sortOrder = 3)
        val subPaper2 = Subject("sub_p2", "paper_02", "Science", "SCI", "", isActive = true, sortOrder = 1)
        repository.saveSubjects(listOf(sub1, sub2, subInactive, subPaper2))

        val activeSubjects = repository.observeActiveSubjectsByPaperId("paper_01").first()
            .sortedWith(compareBy({ it.sortOrder }, { it.name }))

        assertEquals(2, activeSubjects.size)
        assertEquals("sub_02", activeSubjects[0].id)
        assertEquals("sub_01", activeSubjects[1].id)
        assertFalse(activeSubjects.any { it.id == "sub_inact" })
        assertFalse(activeSubjects.any { it.id == "sub_p2" })
    }

    @Test
    fun topics_filteredBySelectedSubject_andInactiveHidden() = runBlocking {
        val exam = Exam("exam_01", "CTET", "CTET", "", true, 1)
        val paper = Paper("paper_01", "exam_01", "Paper 1", "P1", "", true, 1)
        val subject = Subject("subject_01", "paper_01", "CDP", "CDP", "", true, 1)
        repository.saveExam(exam)
        repository.savePaper(paper)
        repository.saveSubject(subject)

        val top1 = Topic("top_01", "subject_01", "Principles of Child Development", "", isActive = true, sortOrder = 1)
        val top2 = Topic("top_02", "subject_01", "Influence of Heredity & Environment", "", isActive = true, sortOrder = 2)
        val topInactive = Topic("top_inact", "subject_01", "Inactive Topic", "", isActive = false, sortOrder = 3)
        repository.saveTopics(listOf(top1, top2, topInactive))

        val activeTopics = repository.observeActiveTopicsBySubjectId("subject_01").first()
            .sortedWith(compareBy({ it.sortOrder }, { it.name }))

        assertEquals(2, activeTopics.size)
        assertEquals("top_01", activeTopics[0].id)
        assertEquals("top_02", activeTopics[1].id)
        assertFalse(activeTopics.any { it.id == "top_inact" })
    }

    @Test
    fun subtopicsWithDetails_enrichesMetadata_andAccurateActiveQuestionCount() = runBlocking {
        val exam = Exam("exam_01", "CTET", "CTET", "", true, 1)
        val paper = Paper("paper_01", "exam_01", "Paper 1", "P1", "", true, 1)
        val subject = Subject("subject_01", "paper_01", "CDP", "CDP", "", true, 1)
        val topic = Topic("top_01", "subject_01", "Development", "", true, 1)
        val subtopic1 = Subtopic("subtop_01", "top_01", "Piaget Theory", "Cognitive stages", isActive = true, sortOrder = 1)
        val subtopic2 = Subtopic("subtop_02", "top_01", "Vygotsky Theory", "Sociocultural theory", isActive = true, sortOrder = 2)
        val subtopicInactive = Subtopic("subtop_inact", "top_01", "Inactive Subtopic", "", isActive = false, sortOrder = 3)

        repository.saveExam(exam)
        repository.savePaper(paper)
        repository.saveSubject(subject)
        repository.saveTopic(topic)
        repository.saveSubtopics(listOf(subtopic1, subtopic2, subtopicInactive))

        // Save metadata for subtopic1
        val meta1 = SyllabusMetadata(
            nodeId = "subtop_01",
            learningObjective = "Understand sensorimotor, preoperational, concrete, formal operational stages",
            shortNote = "Key concepts: assimilation, accommodation, equilibration",
            estimatedMinutes = 25
        )
        repository.saveSyllabusMetadata(meta1)

        // Save 2 active questions and 1 inactive question under subtopic1
        val q1 = Question(
            id = "q1",
            subtopicId = "subtop_01",
            questionText = "According to Piaget, during which stage does object permanence develop?",
            questionType = QuestionType.MCQ_SINGLE,
            difficulty = QuestionDifficulty.EASY,
            isActive = true,
            sortOrder = 1,
            options = listOf(
                QuestionOption("opt1_1", "q1", "Sensorimotor", 0, true),
                QuestionOption("opt1_2", "q1", "Preoperational", 1, false)
            )
        )
        val q2 = Question(
            id = "q2",
            subtopicId = "subtop_01",
            questionText = "What is schema according to Jean Piaget?",
            questionType = QuestionType.MCQ_SINGLE,
            difficulty = QuestionDifficulty.MEDIUM,
            isActive = true,
            sortOrder = 2,
            options = listOf(
                QuestionOption("opt2_1", "q2", "Building block of knowledge", 0, true),
                QuestionOption("opt2_2", "q2", "A zone of development", 1, false)
            )
        )
        val qInactive = Question(
            id = "q_inact",
            subtopicId = "subtop_01",
            questionText = "Draft question not yet published?",
            questionType = QuestionType.MCQ_SINGLE,
            difficulty = QuestionDifficulty.HARD,
            isActive = false, // INACTIVE question
            sortOrder = 3,
            options = listOf(
                QuestionOption("opt3_1", "q_inact", "Option A", 0, true),
                QuestionOption("opt3_2", "q_inact", "Option B", 1, false)
            )
        )
        repository.saveQuestions(listOf(q1, q2, qInactive))

        // Observe SubtopicWithDetails
        val subtopicsWithDetails = repository.observeActiveSubtopicsWithDetailsByTopicId("top_01").first()

        assertEquals(2, subtopicsWithDetails.size)

        // Subtopic 1
        val item1 = subtopicsWithDetails[0]
        assertEquals("subtop_01", item1.subtopic.id)
        assertNotNull(item1.metadata)
        assertEquals("Understand sensorimotor, preoperational, concrete, formal operational stages", item1.metadata?.learningObjective)
        assertEquals(25, item1.metadata?.estimatedMinutes)
        assertEquals(2, item1.questionCount) // Exactly 2 active questions, inactive question excluded
        assertTrue(item1.hasQuestions)

        // Subtopic 2
        val item2 = subtopicsWithDetails[1]
        assertEquals("subtop_02", item2.subtopic.id)
        assertNull(item2.metadata)
        assertEquals(0, item2.questionCount)
        assertFalse(item2.hasQuestions)
    }

    @Test
    fun breadcrumb_resolvesAccuratelyFromSubtopicToExam() = runBlocking {
        val exam = Exam("exam_01", "Central Teacher Eligibility Test", "CTET", "", true, 1)
        val paper = Paper("paper_01", "exam_01", "Paper 1 (Class I to V)", "P1", "", true, 1)
        val subject = Subject("subject_01", "paper_01", "Child Development & Pedagogy", "CDP", "", true, 1)
        val topic = Topic("top_01", "subject_01", "Concept of Development", "", true, 1)
        val subtopic = Subtopic("subtop_01", "top_01", "Growth vs Development", "", true, 1)

        repository.saveExam(exam)
        repository.savePaper(paper)
        repository.saveSubject(subject)
        repository.saveTopic(topic)
        repository.saveSubtopic(subtopic)

        val breadcrumb = repository.getSyllabusBreadcrumb("subtop_01", SyllabusNodeType.SUBTOPIC)

        assertNotNull(breadcrumb.exam)
        assertEquals("exam_01", breadcrumb.exam?.id)
        assertEquals("Central Teacher Eligibility Test", breadcrumb.exam?.title)

        assertNotNull(breadcrumb.paper)
        assertEquals("paper_01", breadcrumb.paper?.id)
        assertEquals("Paper 1 (Class I to V)", breadcrumb.paper?.title)

        assertNotNull(breadcrumb.subject)
        assertEquals("subject_01", breadcrumb.subject?.id)
        assertEquals("Child Development & Pedagogy", breadcrumb.subject?.title)

        assertNotNull(breadcrumb.topic)
        assertEquals("top_01", breadcrumb.topic?.id)
        assertEquals("Concept of Development", breadcrumb.topic?.title)

        assertNotNull(breadcrumb.subtopic)
        assertEquals("subtop_01", breadcrumb.subtopic?.id)
        assertEquals("Growth vs Development", breadcrumb.subtopic?.title)
    }

    @Test
    fun emptyState_whenNoChildrenExist() = runBlocking {
        val exam = Exam("exam_empty", "Empty Exam", "EMPTY", "", true, 1)
        repository.saveExam(exam)

        val papers = repository.observeActivePapersByExamId("exam_empty").first()
        assertTrue(papers.isEmpty())
    }
}
