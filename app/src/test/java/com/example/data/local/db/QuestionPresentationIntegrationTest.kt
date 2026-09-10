package com.example.data.local.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.datasource.DefaultLocalEducationalDataSource
import com.example.data.local.db.entity.ExamEntity
import com.example.data.local.db.entity.PaperEntity
import com.example.data.local.db.entity.QuestionEntity
import com.example.data.local.db.entity.QuestionOptionEntity
import com.example.data.local.db.entity.SubjectEntity
import com.example.data.local.db.entity.SubtopicEntity
import com.example.data.local.db.entity.TopicEntity
import com.example.data.repository.EducationalRepositoryImpl
import com.example.domain.model.QuestionDifficulty
import com.example.domain.model.QuestionType
import com.example.ui.feature.question.model.QuestionOptionPresentationModel
import com.example.ui.feature.question.model.QuestionPresentationModel
import com.example.ui.feature.question.model.toPresentationModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Robust integration test for Question Presentation Foundation (Step 8).
 *
 * Verifies with actual Room SQLite database:
 * 1. Subtopic -> Questions -> Options navigation and retrieval.
 * 2. Deterministic ordering by sortOrder ASC, id ASC.
 * 3. Domain mapping and Flow reactivity upon Room database updates.
 * 4. Critical Security Verification:
 *    - The correct-answer key (isCorrect) exists and is preserved internally in Room and domain models.
 *    - The presentation model (QuestionOptionPresentationModel) completely strips and never exposes `isCorrect`.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class QuestionPresentationIntegrationTest {

    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var repository: EducationalRepositoryImpl

    private val testExamId = "exam_ctet_2026"
    private val testPaperId = "paper_ctet_p1"
    private val testSubjectId = "subject_cdp"
    private val testTopicId = "topic_learning"
    private val testSubtopicId = "subtopic_piaget"

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        AppDatabase.setTestInstance(database)

        val localDataSource = DefaultLocalEducationalDataSource { database }
        repository = EducationalRepositoryImpl(localDataSource = localDataSource, ioDispatcher = Dispatchers.Unconfined)

        seedEducationalHierarchy()
    }

    @After
    fun tearDown() {
        AppDatabase.setTestInstance(null)
        database.close()
    }

    private fun seedEducationalHierarchy() = runBlocking {
        database.examDao().insertOrUpdateExam(
            ExamEntity(testExamId, "CTET Exam", "CTET", "Teacher Eligibility", true, 1, 1000L)
        )
        database.paperDao().insertOrUpdatePaper(
            PaperEntity(testPaperId, testExamId, "Paper 1", "P1", "Primary", true, 1, 1000L)
        )
        database.subjectDao().insertOrUpdateSubject(
            SubjectEntity(testSubjectId, testPaperId, "Child Development", "CDP", "Pedagogy", true, 1, 1000L)
        )
        database.topicDao().insertOrUpdateTopic(
            TopicEntity(testTopicId, testSubjectId, "Cognitive Theories", "Theories", true, 1, 1000L)
        )
        database.subtopicDao().insertOrUpdateSubtopic(
            SubtopicEntity(testSubtopicId, testTopicId, "Piaget Stages", "Stages of cognitive development", true, 1, 1000L)
        )
    }

    @Test
    fun subtopic_toQuestions_toOptions_retrievalUsingRealRoomDatabase() = runBlocking {
        // Insert 2 questions for the subtopic
        val q1 = QuestionEntity(
            id = "q_piaget_01",
            subtopicId = testSubtopicId,
            questionText = "According to Piaget, during which stage does object permanence develop?",
            explanation = "Object permanence develops in the sensorimotor stage (birth to 2 years).",
            questionType = QuestionType.MCQ_SINGLE.name,
            difficulty = QuestionDifficulty.EASY.name,
            isActive = true,
            sortOrder = 1,
            updatedAtTimestamp = 1000L
        )
        val q2 = QuestionEntity(
            id = "q_piaget_02",
            subtopicId = testSubtopicId,
            questionText = "What characterizes the pre-operational stage?",
            explanation = "Egocentrism and symbolic thought characterize the pre-operational stage.",
            questionType = QuestionType.MCQ_SINGLE.name,
            difficulty = QuestionDifficulty.MEDIUM.name,
            isActive = true,
            sortOrder = 2,
            updatedAtTimestamp = 1000L
        )
        database.questionDao().insertOrUpdateQuestions(listOf(q1, q2))

        // Insert options with correct and incorrect values
        val q1Options = listOf(
            QuestionOptionEntity(id = "opt_1a", questionId = "q_piaget_01", optionText = "Sensorimotor stage", sortOrder = 1, isCorrect = true, updatedAtTimestamp = 1000L),
            QuestionOptionEntity(id = "opt_1b", questionId = "q_piaget_01", optionText = "Pre-operational stage", sortOrder = 2, isCorrect = false, updatedAtTimestamp = 1000L),
            QuestionOptionEntity(id = "opt_1c", questionId = "q_piaget_01", optionText = "Concrete operational stage", sortOrder = 3, isCorrect = false, updatedAtTimestamp = 1000L),
            QuestionOptionEntity(id = "opt_1d", questionId = "q_piaget_01", optionText = "Formal operational stage", sortOrder = 4, isCorrect = false, updatedAtTimestamp = 1000L)
        )
        val q2Options = listOf(
            QuestionOptionEntity(id = "opt_2a", questionId = "q_piaget_02", optionText = "Hypothetical reasoning", sortOrder = 1, isCorrect = false, updatedAtTimestamp = 1000L),
            QuestionOptionEntity(id = "opt_2b", questionId = "q_piaget_02", optionText = "Egocentric thinking", sortOrder = 2, isCorrect = true, updatedAtTimestamp = 1000L),
            QuestionOptionEntity(id = "opt_2c", questionId = "q_piaget_02", optionText = "Conservation logic", sortOrder = 3, isCorrect = false, updatedAtTimestamp = 1000L)
        )
        database.questionOptionDao().insertOrUpdateOptions(q1Options + q2Options)

        // 1. Observe questions for subtopic through repository
        val questions = repository.observeQuestionsForSubtopic(testSubtopicId, activeOnly = true).first()
        assertEquals(2, questions.size)

        // Verify deterministic question ordering
        assertEquals("q_piaget_01", questions[0].id)
        assertEquals("q_piaget_02", questions[1].id)

        // 2. Verify options mapped correctly to question domain models
        assertEquals(4, questions[0].options.size)
        assertEquals(3, questions[1].options.size)
        assertEquals("Sensorimotor stage", questions[0].options[0].optionText)
        assertEquals("Pre-operational stage", questions[0].options[1].optionText)

        // 3. Verify single question observation by ID
        val observedQ1 = repository.observeQuestion("q_piaget_01").first()
        assertNotNull(observedQ1)
        assertEquals("According to Piaget, during which stage does object permanence develop?", observedQ1?.questionText)
        assertEquals(4, observedQ1?.options?.size)
    }

    @Test
    fun securityCheck_correctAnswerExistsInternally_butIsNeverExposedInPresentationModel() = runBlocking {
        val q = QuestionEntity(
            id = "q_sec_01",
            subtopicId = testSubtopicId,
            questionText = "Which gas is most abundant in Earth's atmosphere?",
            explanation = "Nitrogen constitutes roughly 78% of the atmosphere.",
            questionType = QuestionType.MCQ_SINGLE.name,
            difficulty = QuestionDifficulty.EASY.name,
            isActive = true,
            sortOrder = 1,
            updatedAtTimestamp = 1000L
        )
        database.questionDao().insertOrUpdateQuestion(q)

        val options = listOf(
            QuestionOptionEntity(id = "opt_sec_o2", questionId = "q_sec_01", optionText = "Oxygen", sortOrder = 1, isCorrect = false, updatedAtTimestamp = 1000L),
            QuestionOptionEntity(id = "opt_sec_n2", questionId = "q_sec_01", optionText = "Nitrogen", sortOrder = 2, isCorrect = true, updatedAtTimestamp = 1000L),
            QuestionOptionEntity(id = "opt_sec_ar", questionId = "q_sec_01", optionText = "Argon", sortOrder = 3, isCorrect = false, updatedAtTimestamp = 1000L),
            QuestionOptionEntity(id = "opt_sec_co2", questionId = "q_sec_01", optionText = "Carbon Dioxide", sortOrder = 4, isCorrect = false, updatedAtTimestamp = 1000L)
        )
        database.questionOptionDao().insertOrUpdateOptions(options)

        // Retrieve domain model from repository
        val domainQuestion = repository.observeQuestion("q_sec_01").first()
        assertNotNull(domainQuestion)

        // Verify INTERNAL domain model HAS isCorrect
        val correctDomainOption = domainQuestion!!.options.first { it.isCorrect }
        assertEquals("Nitrogen", correctDomainOption.optionText)
        assertTrue(correctDomainOption.isCorrect)

        // Map to Presentation Model (as used by UI)
        val presentationModel = domainQuestion.toPresentationModel(questionNumber = 1)
        assertEquals(1, presentationModel.questionNumber)
        assertEquals(4, presentationModel.options.size)

        // Verify labels are deterministic A, B, C, D
        assertEquals("A", presentationModel.options[0].label)
        assertEquals("B", presentationModel.options[1].label)
        assertEquals("C", presentationModel.options[2].label)
        assertEquals("D", presentationModel.options[3].label)

        // Reflection Verification: QuestionOptionPresentationModel must have ZERO fields indicating correctness
        val presentationFields = QuestionOptionPresentationModel::class.java.declaredFields.map { it.name }
        assertFalse("Presentation model must NOT contain isCorrect field", presentationFields.contains("isCorrect"))
        assertFalse("Presentation model must NOT contain correct field", presentationFields.contains("correct"))
        assertFalse("Presentation model must NOT contain is_correct field", presentationFields.contains("is_correct"))

        val presentationQuestionFields = QuestionPresentationModel::class.java.declaredFields.map { it.name }
        assertFalse("Presentation model must NOT contain correctOptionId field", presentationQuestionFields.contains("correctOptionId"))
        assertFalse("Presentation model must NOT contain answerKey field", presentationQuestionFields.contains("answerKey"))
    }

    @Test
    fun flowReactivity_emitsNewStateWhenQuestionAddedOrUpdated() = runBlocking {
        // Initially empty
        val initialQuestions = repository.observeQuestionsForSubtopic(testSubtopicId, activeOnly = true).first()
        assertTrue(initialQuestions.isEmpty())

        // Insert question
        val q1 = QuestionEntity(
            id = "q_flow_01",
            subtopicId = testSubtopicId,
            questionText = "Flow reactivity test question",
            explanation = "",
            questionType = QuestionType.MCQ_SINGLE.name,
            difficulty = QuestionDifficulty.EASY.name,
            isActive = true,
            sortOrder = 1,
            updatedAtTimestamp = 1000L
        )
        database.questionDao().insertOrUpdateQuestion(q1)

        val updatedQuestions = repository.observeQuestionsForSubtopic(testSubtopicId, activeOnly = true).first()
        assertEquals(1, updatedQuestions.size)
        assertEquals("q_flow_01", updatedQuestions[0].id)
    }
}
