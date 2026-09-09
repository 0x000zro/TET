package com.example.data.local.seed

import com.example.domain.model.Exam
import com.example.domain.model.Paper
import com.example.domain.model.Subject
import com.example.domain.model.Subtopic
import com.example.domain.model.Topic

/**
 * Minimal development/demo educational hierarchy seed dataset.
 *
 * Clearly separated for development and local testing verification ONLY.
 * Does NOT hard-code official CTET/UPTET syllabi.
 * Uses completely generic hierarchy nodes demonstrating:
 * Exam -> Paper -> Subject -> Topic -> Subtopic.
 */
object DemoHierarchySeedData {

    val demoExam = Exam(
        id = "demo_exam_01",
        name = "Demo Comprehensive Assessment",
        shortName = "DEMO-EXAM",
        description = "Development demo examination hierarchy node for verifying Room persistence.",
        isActive = true,
        sortOrder = 1
    )

    val demoPapers = listOf(
        Paper(
            id = "demo_paper_01",
            examId = "demo_exam_01",
            name = "Demo General Pedagogy & Core Foundation",
            shortName = "Paper I",
            description = "Fundamental core paper testing pedagogy and foundational concepts.",
            isActive = true,
            sortOrder = 1
        ),
        Paper(
            id = "demo_paper_02",
            examId = "demo_exam_01",
            name = "Demo Specialized Subject Concepts",
            shortName = "Paper II",
            description = "Advanced domain discipline testing applied conceptual reasoning.",
            isActive = true,
            sortOrder = 2
        )
    )

    val demoSubjects = listOf(
        Subject(
            id = "demo_subject_01",
            paperId = "demo_paper_01",
            name = "Demo Educational Psychology",
            shortName = "EduPsych",
            description = "Cognitive development stages, learning theories, and evaluation methods.",
            isActive = true,
            sortOrder = 1
        ),
        Subject(
            id = "demo_subject_02",
            paperId = "demo_paper_01",
            name = "Demo Quantitative Problem Solving",
            shortName = "MathReasoning",
            description = "Numerical fluency, algebraic fundamentals, and arithmetic reasoning.",
            isActive = true,
            sortOrder = 2
        )
    )

    val demoTopics = listOf(
        Topic(
            id = "demo_topic_01",
            subjectId = "demo_subject_01",
            name = "Demo Cognitive Growth & Development",
            description = "Key principles of cognitive progression across early to adolescent stages.",
            isActive = true,
            sortOrder = 1
        ),
        Topic(
            id = "demo_topic_02",
            subjectId = "demo_subject_01",
            name = "Demo Learning Theories & Motivation",
            description = "Constructivist, behavioral, and experiential learning paradigms.",
            isActive = true,
            sortOrder = 2
        )
    )

    val demoSubtopics = listOf(
        Subtopic(
            id = "demo_subtopic_01",
            topicId = "demo_topic_01",
            name = "Demo Stages of Intellectual Development",
            description = "Detailed stages, sensorimotor transitions, and schema formulation.",
            isActive = true,
            sortOrder = 1
        ),
        Subtopic(
            id = "demo_subtopic_02",
            topicId = "demo_topic_01",
            name = "Demo Socio-Cultural Influences on Learning",
            description = "Zone of proximal development, scaffolding, and collaborative peer learning.",
            isActive = true,
            sortOrder = 2
        )
    )
}
