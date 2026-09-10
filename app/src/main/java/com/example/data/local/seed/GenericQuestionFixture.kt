package com.example.data.local.seed

import com.example.domain.model.Question
import com.example.domain.model.QuestionDifficulty
import com.example.domain.model.QuestionOption
import com.example.domain.model.QuestionType

/**
 * Minimal fixture builder providing valid educational questions and options
 * for testing and foundational validation without bloated question banks.
 */
object GenericQuestionFixture {

    fun createSampleQuestion(
        id: String = "q_sample_01",
        subtopicId: String = "subtopic_piaget_stages",
        questionText: String = "According to Piaget, in which stage does a child develop object permanence?",
        difficulty: QuestionDifficulty = QuestionDifficulty.MEDIUM,
        explanation: String = "Sensorimotor stage occurs from birth to approximately 2 years, during which object permanence develops.",
        isActive: Boolean = true,
        sortOrder: Int = 1
    ): Question {
        return Question(
            id = id,
            subtopicId = subtopicId,
            questionText = questionText,
            questionType = QuestionType.MCQ_SINGLE,
            difficulty = difficulty,
            explanation = explanation,
            isActive = isActive,
            sortOrder = sortOrder,
            options = listOf(
                QuestionOption(
                    id = "${id}_opt_a",
                    questionId = id,
                    optionText = "Sensorimotor stage",
                    sortOrder = 1,
                    isCorrect = true
                ),
                QuestionOption(
                    id = "${id}_opt_b",
                    questionId = id,
                    optionText = "Preoperational stage",
                    sortOrder = 2,
                    isCorrect = false
                ),
                QuestionOption(
                    id = "${id}_opt_c",
                    questionId = id,
                    optionText = "Concrete operational stage",
                    sortOrder = 3,
                    isCorrect = false
                ),
                QuestionOption(
                    id = "${id}_opt_d",
                    questionId = id,
                    optionText = "Formal operational stage",
                    sortOrder = 4,
                    isCorrect = false
                )
            )
        )
    }

    fun createSampleQuestionsForSubtopic(subtopicId: String, count: Int = 3): List<Question> {
        return (1..count).map { i ->
            Question(
                id = "q_${subtopicId}_$i",
                subtopicId = subtopicId,
                questionText = "Sample foundational question #$i for subtopic $subtopicId?",
                questionType = QuestionType.MCQ_SINGLE,
                difficulty = when (i % 3) {
                    1 -> QuestionDifficulty.EASY
                    2 -> QuestionDifficulty.MEDIUM
                    else -> QuestionDifficulty.HARD
                },
                explanation = "Foundation explanation for question #$i.",
                isActive = true,
                sortOrder = i,
                options = listOf(
                    QuestionOption(
                        id = "q_${subtopicId}_${i}_opt_1",
                        questionId = "q_${subtopicId}_$i",
                        optionText = "Option 1 (Correct)",
                        sortOrder = 1,
                        isCorrect = true
                    ),
                    QuestionOption(
                        id = "q_${subtopicId}_${i}_opt_2",
                        questionId = "q_${subtopicId}_$i",
                        optionText = "Option 2",
                        sortOrder = 2,
                        isCorrect = false
                    ),
                    QuestionOption(
                        id = "q_${subtopicId}_${i}_opt_3",
                        questionId = "q_${subtopicId}_$i",
                        optionText = "Option 3",
                        sortOrder = 3,
                        isCorrect = false
                    ),
                    QuestionOption(
                        id = "q_${subtopicId}_${i}_opt_4",
                        questionId = "q_${subtopicId}_$i",
                        optionText = "Option 4",
                        sortOrder = 4,
                        isCorrect = false
                    )
                )
            )
        }
    }
}
