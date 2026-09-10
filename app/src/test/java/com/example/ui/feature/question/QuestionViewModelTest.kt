package com.example.ui.feature.question

import com.example.domain.model.AppFoundationInfo
import com.example.domain.model.AppState
import com.example.domain.model.ContentSyncState
import com.example.domain.model.EducationalModule
import com.example.domain.model.Exam
import com.example.domain.model.LocalPreference
import com.example.domain.model.Paper
import com.example.domain.model.Question
import com.example.domain.model.QuestionDifficulty
import com.example.domain.model.QuestionOption
import com.example.domain.model.Subject
import com.example.domain.model.Subtopic
import com.example.domain.model.SubtopicWithDetails
import com.example.domain.model.SyllabusBreadcrumb
import com.example.domain.model.SyllabusMetadata
import com.example.domain.model.SyllabusNode
import com.example.domain.model.SyllabusNodeType
import com.example.domain.model.SyllabusTreeNode
import com.example.domain.model.Topic
import com.example.domain.repository.EducationalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QuestionViewModelTest {

    private val testDispatcher: TestDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeEducationalRepository
    private lateinit var viewModel: QuestionViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeEducationalRepository()
        viewModel = QuestionViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadQuestions_success_loadsQuestionsCorrectly() = runTest(testDispatcher) {
        val q1 = createTestQuestion(id = "q1", subtopicId = "sub1", sortOrder = 1, isActive = true)
        val q2 = createTestQuestion(id = "q2", subtopicId = "sub1", sortOrder = 2, isActive = true)
        fakeRepository.questionsFlow.value = listOf(q1, q2)

        viewModel.loadQuestionsForSubtopic("sub1")
        advanceUntilIdle()

        val state = viewModel.listState.value
        assertTrue(state is QuestionListUiState.Success)
        val questions = (state as QuestionListUiState.Success).questions
        assertEquals(2, questions.size)
        assertEquals("q1", questions[0].id)
        assertEquals(1, questions[0].questionNumber)
        assertEquals("q2", questions[1].id)
        assertEquals(2, questions[1].questionNumber)
    }

    @Test
    fun loadQuestions_filtersInactiveQuestions_onlyActiveQuestionsAppear() = runTest(testDispatcher) {
        val qActive = createTestQuestion(id = "q_active", subtopicId = "sub1", isActive = true)
        val qInactive = createTestQuestion(id = "q_inactive", subtopicId = "sub1", isActive = false)
        fakeRepository.questionsFlow.value = listOf(qActive, qInactive)

        viewModel.loadQuestionsForSubtopic("sub1")
        advanceUntilIdle()

        val state = viewModel.listState.value
        assertTrue(state is QuestionListUiState.Success)
        val questions = (state as QuestionListUiState.Success).questions
        assertEquals(1, questions.size)
        assertEquals("q_active", questions[0].id)
    }

    @Test
    fun loadQuestions_deterministicOrdering_sortsBySortOrderThenId() = runTest(testDispatcher) {
        val q3 = createTestQuestion(id = "q3", subtopicId = "sub1", sortOrder = 3)
        val q1 = createTestQuestion(id = "q1", subtopicId = "sub1", sortOrder = 1)
        val q2b = createTestQuestion(id = "q2_b", subtopicId = "sub1", sortOrder = 2)
        val q2a = createTestQuestion(id = "q2_a", subtopicId = "sub1", sortOrder = 2)
        fakeRepository.questionsFlow.value = listOf(q3, q1, q2b, q2a)

        viewModel.loadQuestionsForSubtopic("sub1")
        advanceUntilIdle()

        val state = viewModel.listState.value
        assertTrue(state is QuestionListUiState.Success)
        val questions = (state as QuestionListUiState.Success).questions
        assertEquals(4, questions.size)
        assertEquals("q1", questions[0].id)
        assertEquals("q2_a", questions[1].id)
        assertEquals("q2_b", questions[2].id)
        assertEquals("q3", questions[3].id)
    }

    @Test
    fun loadQuestions_empty_emitsEmptyState() = runTest(testDispatcher) {
        fakeRepository.questionsFlow.value = emptyList()

        viewModel.loadQuestionsForSubtopic("sub1")
        advanceUntilIdle()

        assertEquals(QuestionListUiState.Empty, viewModel.listState.value)
    }

    @Test
    fun loadQuestions_repositoryError_emitsErrorState() = runTest(testDispatcher) {
        fakeRepository.throwQuestionsError = true

        viewModel.loadQuestionsForSubtopic("sub1")
        advanceUntilIdle()

        val state = viewModel.listState.value
        assertTrue(state is QuestionListUiState.Error)
        assertEquals("Repository simulated error", (state as QuestionListUiState.Error).message)
    }

    @Test
    fun retryQuestions_reloadsQuestionsForCurrentSubtopic() = runTest(testDispatcher) {
        fakeRepository.throwQuestionsError = true
        viewModel.loadQuestionsForSubtopic("sub1")
        advanceUntilIdle()
        assertTrue(viewModel.listState.value is QuestionListUiState.Error)

        // Clear error and provide questions
        fakeRepository.throwQuestionsError = false
        val q1 = createTestQuestion(id = "q1", subtopicId = "sub1")
        fakeRepository.questionsFlow.value = listOf(q1)

        viewModel.retryQuestions()
        advanceUntilIdle()

        val state = viewModel.listState.value
        assertTrue(state is QuestionListUiState.Success)
        assertEquals(1, (state as QuestionListUiState.Success).questions.size)
    }

    @Test
    fun loadQuestionDetail_success_loadsDetailCorrectly() = runTest(testDispatcher) {
        val q = createTestQuestion(
            id = "q_detail",
            subtopicId = "sub1",
            text = "What is cognitive development?",
            explanation = "Piaget developed cognitive theory.",
            options = listOf(
                QuestionOption("opt1", "q_detail", "Option 1", sortOrder = 1, isCorrect = true),
                QuestionOption("opt2", "q_detail", "Option 2", sortOrder = 2, isCorrect = false)
            )
        )
        fakeRepository.questionDetailFlow.value = q

        viewModel.loadQuestionDetail("q_detail", questionIndex = 3)
        advanceUntilIdle()

        val state = viewModel.detailState.value
        assertTrue(state is QuestionDetailUiState.Success)
        val presentation = (state as QuestionDetailUiState.Success).question
        assertEquals("q_detail", presentation.id)
        assertEquals(3, presentation.questionNumber)
        assertEquals("What is cognitive development?", presentation.questionText)
        assertEquals("Piaget developed cognitive theory.", presentation.explanation)
        assertEquals(2, presentation.options.size)
        assertEquals("A", presentation.options[0].label)
        assertEquals("Option 1", presentation.options[0].optionText)
        assertEquals("B", presentation.options[1].label)
        assertEquals("Option 2", presentation.options[1].optionText)
    }

    @Test
    fun loadQuestionDetail_missingOrInactive_emitsNotFoundState() = runTest(testDispatcher) {
        // Missing question
        fakeRepository.questionDetailFlow.value = null
        viewModel.loadQuestionDetail("q_non_existent")
        advanceUntilIdle()
        assertEquals(QuestionDetailUiState.NotFound, viewModel.detailState.value)

        // Inactive question
        val inactiveQ = createTestQuestion(id = "q_inactive", isActive = false)
        fakeRepository.questionDetailFlow.value = inactiveQ
        viewModel.loadQuestionDetail("q_inactive")
        advanceUntilIdle()
        assertEquals(QuestionDetailUiState.NotFound, viewModel.detailState.value)
    }

    @Test
    fun loadQuestionDetail_repositoryError_emitsErrorState() = runTest(testDispatcher) {
        fakeRepository.throwQuestionDetailError = true

        viewModel.loadQuestionDetail("q1")
        advanceUntilIdle()

        val state = viewModel.detailState.value
        assertTrue(state is QuestionDetailUiState.Error)
    }

    @Test
    fun retryQuestionDetail_reloadsDetailForCurrentQuestion() = runTest(testDispatcher) {
        fakeRepository.throwQuestionDetailError = true
        viewModel.loadQuestionDetail("q1", questionIndex = 2)
        advanceUntilIdle()
        assertTrue(viewModel.detailState.value is QuestionDetailUiState.Error)

        fakeRepository.throwQuestionDetailError = false
        val q1 = createTestQuestion(id = "q1")
        fakeRepository.questionDetailFlow.value = q1

        viewModel.retryQuestionDetail()
        advanceUntilIdle()

        val state = viewModel.detailState.value
        assertTrue(state is QuestionDetailUiState.Success)
        assertEquals(2, (state as QuestionDetailUiState.Success).question.questionNumber)
    }

    private fun createTestQuestion(
        id: String,
        subtopicId: String = "sub_default",
        text: String = "Sample question text",
        explanation: String = "Sample explanation",
        difficulty: QuestionDifficulty = QuestionDifficulty.MEDIUM,
        isActive: Boolean = true,
        sortOrder: Int = 1,
        options: List<QuestionOption> = emptyList()
    ): Question {
        return Question(
            id = id,
            subtopicId = subtopicId,
            questionText = text,
            explanation = explanation,
            difficulty = difficulty,
            isActive = isActive,
            sortOrder = sortOrder,
            options = options
        )
    }

    private class FakeEducationalRepository : com.example.testutil.TestEducationalRepository() {
        val questionsFlow = MutableStateFlow<List<Question>>(emptyList())
        val questionDetailFlow = MutableStateFlow<Question?>(null)
        var throwQuestionsError = false
        var throwQuestionDetailError = false

        override fun observeQuestionsForSubtopic(subtopicId: String, activeOnly: Boolean): Flow<List<Question>> {
            if (throwQuestionsError) {
                return flow { throw RuntimeException("Repository simulated error") }
            }
            return questionsFlow
        }

        override fun observeQuestionById(id: String): Flow<Question?> {
            if (throwQuestionDetailError) {
                return flow { throw RuntimeException("Repository simulated error") }
            }
            return questionDetailFlow
        }

        override fun observeQuestion(questionId: String): Flow<Question?> = observeQuestionById(questionId)
    }
}
