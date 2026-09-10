package com.example.ui.feature.bookmark

import com.example.domain.model.Question
import com.example.domain.model.QuestionOption
import com.example.domain.model.bookmark.BookmarkedQuestion
import com.example.testutil.TestEducationalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BookmarkedQuestionsViewModelTest {

    private val testDispatcher: TestDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: TestEducationalRepository
    private lateinit var viewModel: BookmarkedQuestionsViewModel

    private val testQuestion1 = Question(
        id = "q_1",
        subtopicId = "sub_1",
        questionText = "What is 2 + 2?",
        options = listOf(
            QuestionOption("opt_1", "q_1", "3", 1, false),
            QuestionOption("opt_2", "q_1", "4", 2, true)
        )
    )

    private val testQuestion2 = Question(
        id = "q_2",
        subtopicId = "sub_2",
        questionText = "What is 3 * 3?",
        options = listOf(
            QuestionOption("opt_3", "q_2", "9", 1, true),
            QuestionOption("opt_4", "q_2", "6", 2, false)
        )
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = TestEducationalRepository()
        fakeRepository.questionsMap[testQuestion1.id] = testQuestion1
        fakeRepository.questionsMap[testQuestion2.id] = testQuestion2
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state before coroutines finish is Loading`() {
        viewModel = BookmarkedQuestionsViewModel(repository = fakeRepository)
        assertEquals(BookmarkedQuestionsUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `empty bookmarks produces Empty state`() = runTest(testDispatcher) {
        viewModel = BookmarkedQuestionsViewModel(repository = fakeRepository)
        advanceUntilIdle()

        assertEquals(BookmarkedQuestionsUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun `recorded bookmarks produce Success state with mapped question summaries`() = runTest(testDispatcher) {
        fakeRepository.bookmarkedQuestions.addAll(
            listOf(
                BookmarkedQuestion(
                    questionId = "q_1",
                    subtopicId = "sub_1",
                    bookmarkedAt = 1000L
                ),
                BookmarkedQuestion(
                    questionId = "q_2",
                    subtopicId = "sub_2",
                    bookmarkedAt = 2000L
                )
            )
        )

        viewModel = BookmarkedQuestionsViewModel(repository = fakeRepository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("Expected Success state, but was $state", state is BookmarkedQuestionsUiState.Success)
        val success = state as BookmarkedQuestionsUiState.Success
        assertEquals(2, success.items.size)
        assertEquals("q_2", success.items[0].question.id) // Ordered by bookmarkedAt DESC
        assertEquals("q_1", success.items[1].question.id)
    }

    @Test
    fun `subtopic filter filters the visible bookmarks`() = runTest(testDispatcher) {
        fakeRepository.bookmarkedQuestions.addAll(
            listOf(
                BookmarkedQuestion("q_1", "sub_1", 1000L),
                BookmarkedQuestion("q_2", "sub_2", 2000L)
            )
        )

        viewModel = BookmarkedQuestionsViewModel(repository = fakeRepository)
        advanceUntilIdle()

        viewModel.filterBySubtopic("sub_1")
        advanceUntilIdle()

        val state = viewModel.uiState.value as BookmarkedQuestionsUiState.Success
        assertEquals("sub_1", state.selectedSubtopicId)
        assertEquals(1, state.items.size)
        assertEquals("q_1", state.items[0].question.id)
    }

    @Test
    fun `selectQuestionForReview sets selectedQuestion and clearSelectedQuestion resets it`() = runTest(testDispatcher) {
        fakeRepository.bookmarkedQuestions.add(
            BookmarkedQuestion("q_1", "sub_1", 1000L)
        )

        viewModel = BookmarkedQuestionsViewModel(repository = fakeRepository)
        advanceUntilIdle()

        val state = viewModel.uiState.value as BookmarkedQuestionsUiState.Success
        val summary = state.items.first()

        viewModel.selectQuestionForReview(summary)
        assertNotNull(viewModel.selectedQuestion.value)
        assertEquals("q_1", viewModel.selectedQuestion.value?.question?.id)

        viewModel.clearSelectedQuestion()
        assertNull(viewModel.selectedQuestion.value)
    }

    @Test
    fun `removeBookmark removes bookmark and clears selection if currently selected`() = runTest(testDispatcher) {
        fakeRepository.bookmarkedQuestions.add(
            BookmarkedQuestion("q_1", "sub_1", 1000L)
        )

        viewModel = BookmarkedQuestionsViewModel(repository = fakeRepository)
        advanceUntilIdle()

        val state = viewModel.uiState.value as BookmarkedQuestionsUiState.Success
        val summary = state.items.first()
        viewModel.selectQuestionForReview(summary)

        viewModel.removeBookmark("q_1")
        advanceUntilIdle()

        assertNull(viewModel.selectedQuestion.value)
        assertEquals(0, fakeRepository.bookmarkedQuestions.size)
        assertEquals(BookmarkedQuestionsUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun `repository error produces Error UI state`() = runTest(testDispatcher) {
        fakeRepository.shouldThrowOnBookmark = true

        viewModel = BookmarkedQuestionsViewModel(repository = fakeRepository)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is BookmarkedQuestionsUiState.Error)
    }
}
