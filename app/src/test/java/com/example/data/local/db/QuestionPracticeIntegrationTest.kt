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
import com.example.domain.model.practice.PracticeEvaluationStatus
import com.example.domain.usecase.practice.EvaluatePracticeAnswerUseCase
import com.example.domain.usecase.practice.PracticeEvaluationOutcome
import com.example.ui.feature.practice.PracticeUiState
import com.example.ui.feature.practice.PracticeViewModel
import com.example.ui.feature.question.model.QuestionOptionPresentationModel
import com.example.ui.feature.question.model.QuestionPresentationModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
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
 * End-to-end integration test for Single Question Practice Engine (Step 9).
 *
 * Verifies with actual Room SQLite database:
 * 1. End-to-end question loading and option presentation.
 * 2. In-memory practice evaluation via [EvaluatePracticeAnswerUseCase].
 * 3. Correct answer submission flow -> semantic CORRECT status, reveals correct option and explanation.
 * 4. Incorrect answer submission flow -> semantic INCORRECT status, reveals correct option and explanation.
 * 5. ViewModel lifecycle and state transitions with real Room database data.
 * 6. Non-persistence rule: verifying Room database entities remain unmutated (no speculative persistence).
 * 7. Security verification: presentation model does NOT expose answer key prior to submission.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
@OptIn(ExperimentalCoroutinesApi::class)
class QuestionPracticeIntegrationTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var repository: EducationalRepositoryImpl
    private lateinit var evaluateUseCase: EvaluatePracticeAnswerUseCase

    private val testExamId = "exam_upsc_prelims"
    private val testPaperId = "paper_gs1"
    private val testSubjectId = "subject_polity"
    private val testTopicId = "topic_preamble"
    private val testSubtopicId = "subtopic_sovereignty"
    private val testQuestionId = "q_polity_001"

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        context = ApplicationProvider.getApplicationContext()
        val directExecutor = java.util.concurrent.Executor { it.run() }
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .setQueryExecutor(directExecutor)
            .setTransactionExecutor(directExecutor)
            .build()

        val localDataSource = DefaultLocalEducationalDataSource { database }
        repository = EducationalRepositoryImpl(localDataSource = localDataSource, ioDispatcher = Dispatchers.Unconfined)
        evaluateUseCase = EvaluatePracticeAnswerUseCase(repository)

        seedDatabaseHierarchy()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        database.close()
    }

    private fun seedDatabaseHierarchy() = runBlocking {
        database.examDao().insertOrUpdateExam(
            ExamEntity(id = testExamId, name = "UPSC Prelims", shortName = "UPSC", sortOrder = 1)
        )
        database.paperDao().insertOrUpdatePaper(
            PaperEntity(id = testPaperId, examId = testExamId, name = "General Studies I", shortName = "GS1", sortOrder = 1)
        )
        database.subjectDao().insertOrUpdateSubject(
            SubjectEntity(id = testSubjectId, paperId = testPaperId, name = "Indian Polity", shortName = "Polity", sortOrder = 1)
        )
        database.topicDao().insertOrUpdateTopic(
            TopicEntity(id = testTopicId, subjectId = testSubjectId, name = "The Preamble", sortOrder = 1)
        )
        database.subtopicDao().insertOrUpdateSubtopic(
            SubtopicEntity(id = testSubtopicId, topicId = testTopicId, name = "Sovereign Socialist Secular Democratic Republic", sortOrder = 1)
        )

        // Seed 1 active MCQ question with 4 options (Option B is correct)
        database.questionDao().insertOrUpdateQuestion(
            QuestionEntity(
                id = testQuestionId,
                subtopicId = testSubtopicId,
                questionText = "Which amendment added the words 'Socialist' and 'Secular' to the Preamble?",
                explanation = "The 42nd Constitutional Amendment Act of 1976 added Socialist, Secular, and Integrity.",
                difficulty = QuestionDifficulty.MEDIUM.name,
                questionType = QuestionType.MCQ_SINGLE.name,
                sortOrder = 1,
                isActive = true
            )
        )

        database.questionOptionDao().insertOrUpdateOptions(
            listOf(
                QuestionOptionEntity(id = "opt_a", questionId = testQuestionId, optionText = "44th Amendment Act", isCorrect = false, sortOrder = 1),
                QuestionOptionEntity(id = "opt_b", questionId = testQuestionId, optionText = "42nd Amendment Act", isCorrect = true, sortOrder = 2),
                QuestionOptionEntity(id = "opt_c", questionId = testQuestionId, optionText = "52nd Amendment Act", isCorrect = false, sortOrder = 3),
                QuestionOptionEntity(id = "opt_d", questionId = testQuestionId, optionText = "86th Amendment Act", isCorrect = false, sortOrder = 4)
            )
        )
    }

    @Test
    fun practiceEvaluation_correctOption_evaluatesSuccessfully() = runTest(testDispatcher) {
        val outcome = evaluateUseCase.execute(
            questionId = testQuestionId,
            selectedOptionId = "opt_b"
        )

        assertTrue(outcome is PracticeEvaluationOutcome.Success)
        val result = (outcome as PracticeEvaluationOutcome.Success).result
        assertEquals(testQuestionId, result.questionId)
        assertEquals("opt_b", result.selectedOptionId)
        assertEquals("opt_b", result.correctOptionId)
        assertEquals(PracticeEvaluationStatus.CORRECT, result.status)
        assertEquals("The 42nd Constitutional Amendment Act of 1976 added Socialist, Secular, and Integrity.", result.explanation)
    }

    @Test
    fun practiceEvaluation_incorrectOption_evaluatesSuccessfullyAndRevealsCorrect() = runTest(testDispatcher) {
        val outcome = evaluateUseCase.execute(
            questionId = testQuestionId,
            selectedOptionId = "opt_a"
        )

        assertTrue(outcome is PracticeEvaluationOutcome.Success)
        val result = (outcome as PracticeEvaluationOutcome.Success).result
        assertEquals(testQuestionId, result.questionId)
        assertEquals("opt_a", result.selectedOptionId)
        assertEquals("opt_b", result.correctOptionId)
        assertEquals(PracticeEvaluationStatus.INCORRECT, result.status)
        assertEquals("The 42nd Constitutional Amendment Act of 1976 added Socialist, Secular, and Integrity.", result.explanation)
    }

    @Test
    fun practiceViewModel_withRealDatabase_endToEndPracticeFlow() = runTest(testDispatcher) {
        val viewModel = PracticeViewModel(repository, evaluateUseCase)

        viewModel.loadQuestion(testQuestionId, questionIndex = 1)
        advanceUntilIdle()

        val readyState = viewModel.uiState.value
        assertTrue("State should be Ready", readyState is PracticeUiState.Ready)
        val ready = readyState as PracticeUiState.Ready
        assertEquals(testQuestionId, ready.question.id)
        assertEquals(4, ready.question.options.size)
        assertFalse(ready.canSubmit)

        // Select Option B (the correct one)
        viewModel.selectOption("opt_b")
        val updatedReady = viewModel.uiState.value as PracticeUiState.Ready
        assertEquals("opt_b", updatedReady.selectedOptionId)
        assertTrue(updatedReady.canSubmit)

        // Submit
        viewModel.submitAnswer()
        advanceUntilIdle()

        val submittedState = viewModel.uiState.value
        assertTrue("State should be Submitted", submittedState is PracticeUiState.Submitted)
        val submitted = submittedState as PracticeUiState.Submitted
        assertEquals("opt_b", submitted.selectedOptionId)
        assertEquals(PracticeEvaluationStatus.CORRECT, submitted.result.status)
        assertEquals("opt_b", submitted.result.correctOptionId)

        // Confirm database entities remained completely unchanged (in-memory practice)
        val questionFromDb = database.questionDao().getQuestionById(testQuestionId)
        assertNotNull(questionFromDb)
        assertEquals(1, database.questionDao().getQuestionCountBySubtopicId(testSubtopicId))
        assertEquals(4, database.questionOptionDao().getOptionsForQuestion(testQuestionId).size)
    }

    @Test
    fun securityCheck_presentationModelsDoNotContainAnswerKey() {
        val optionPresentationClass = QuestionOptionPresentationModel::class.java
        val fieldNames = optionPresentationClass.declaredFields.map { it.name }
        assertFalse("QuestionOptionPresentationModel must NOT contain isCorrect field", fieldNames.contains("isCorrect"))
        assertFalse("QuestionOptionPresentationModel must NOT contain correctOptionId field", fieldNames.contains("correctOptionId"))

        val questionPresentationClass = QuestionPresentationModel::class.java
        val qFieldNames = questionPresentationClass.declaredFields.map { it.name }
        assertFalse("QuestionPresentationModel must NOT contain answerKey field", qFieldNames.contains("answerKey"))
        assertFalse("QuestionPresentationModel must NOT contain correctOptionId field", qFieldNames.contains("correctOptionId"))
    }
}
