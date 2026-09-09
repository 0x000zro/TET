package com.example.data.local.seed

import com.example.domain.model.Exam
import com.example.domain.model.Paper
import com.example.domain.model.Subject
import com.example.domain.model.Subtopic
import com.example.domain.model.SyllabusMetadata
import com.example.domain.model.SyllabusNode
import com.example.domain.model.SyllabusNodeType
import com.example.domain.model.Topic

/**
 * Generic development/test fixture providing structured syllabus sample data.
 *
 * Isolated for testing, architectural verification, and developer previews only.
 * Does NOT contain official CTET/UPTET syllabus content.
 * Strictly adheres to clean generic hierarchy modeling.
 */
object GenericSyllabusFixture {

    val fixtureExam = Exam(
        id = "fixture_exam_foundation",
        name = "Foundation Qualifying Examination",
        shortName = "FQE-2026",
        description = "Standard evaluation program covering general foundational and discipline-specific competencies.",
        isActive = true,
        sortOrder = 1
    )

    val fixturePaper = Paper(
        id = "fixture_paper_general",
        examId = "fixture_exam_foundation",
        name = "General Conceptual Foundations",
        shortName = "Paper I",
        description = "Core competencies including general pedagogy, reasoning, and conceptual foundations.",
        isActive = true,
        sortOrder = 1
    )

    val fixtureSubject = Subject(
        id = "fixture_subject_pedagogy",
        paperId = "fixture_paper_general",
        name = "Foundations of Learning and Development",
        shortName = "LearningDev",
        description = "Principles of human cognitive growth, learning mechanics, and evaluation strategies.",
        isActive = true,
        sortOrder = 1
    )

    val fixtureTopic = Topic(
        id = "fixture_topic_cognitive",
        subjectId = "fixture_subject_pedagogy",
        name = "Cognitive Development Frameworks",
        description = "Theoretical paradigms explaining cognitive growth and structured concept acquisition.",
        isActive = true,
        sortOrder = 1
    )

    val fixtureSubtopics = listOf(
        Subtopic(
            id = "fixture_subtopic_sensorimotor",
            topicId = "fixture_topic_cognitive",
            name = "Sensorimotor to Operational Thinking",
            description = "Transitions from sensory schema formation into operational concrete reasoning.",
            isActive = true,
            sortOrder = 1
        ),
        Subtopic(
            id = "fixture_subtopic_scaffolding",
            topicId = "fixture_topic_cognitive",
            name = "Scaffolding and Zone of Proximal Development",
            description = "Instructional mediation, peer dialogue, and structured pedagogical assistance.",
            isActive = true,
            sortOrder = 2
        )
    )

    val fixtureMetadataList = listOf(
        SyllabusMetadata(
            nodeId = "fixture_subtopic_sensorimotor",
            learningObjective = "Identify the milestone transitions between pre-operational and concrete operational thought.",
            shortNote = "Key focus: conservation experiments and egocentrism resolution.",
            estimatedMinutes = 45
        ),
        SyllabusMetadata(
            nodeId = "fixture_subtopic_scaffolding",
            learningObjective = "Analyze teacher-facilitated scaffolding techniques that bridge guided to independent mastery.",
            shortNote = "Focus: ZPD upper limits and dynamic assessment.",
            estimatedMinutes = 40
        )
    )

    /**
     * Pre-mapped syllabus nodes for standalone domain and validation tests.
     */
    val fixtureSyllabusNodes: List<SyllabusNode> by lazy {
        listOf(
            SyllabusNode(
                id = fixtureExam.id,
                parentId = null,
                title = fixtureExam.name,
                description = fixtureExam.description,
                nodeType = SyllabusNodeType.EXAM,
                isActive = fixtureExam.isActive,
                sortOrder = fixtureExam.sortOrder
            ),
            SyllabusNode(
                id = fixturePaper.id,
                parentId = fixturePaper.examId,
                title = fixturePaper.name,
                description = fixturePaper.description,
                nodeType = SyllabusNodeType.PAPER,
                isActive = fixturePaper.isActive,
                sortOrder = fixturePaper.sortOrder
            ),
            SyllabusNode(
                id = fixtureSubject.id,
                parentId = fixtureSubject.paperId,
                title = fixtureSubject.name,
                description = fixtureSubject.description,
                nodeType = SyllabusNodeType.SUBJECT,
                isActive = fixtureSubject.isActive,
                sortOrder = fixtureSubject.sortOrder
            ),
            SyllabusNode(
                id = fixtureTopic.id,
                parentId = fixtureTopic.subjectId,
                title = fixtureTopic.name,
                description = fixtureTopic.description,
                nodeType = SyllabusNodeType.TOPIC,
                isActive = fixtureTopic.isActive,
                sortOrder = fixtureTopic.sortOrder
            ),
            SyllabusNode(
                id = fixtureSubtopics[0].id,
                parentId = fixtureSubtopics[0].topicId,
                title = fixtureSubtopics[0].name,
                description = fixtureSubtopics[0].description,
                nodeType = SyllabusNodeType.SUBTOPIC,
                isActive = fixtureSubtopics[0].isActive,
                sortOrder = fixtureSubtopics[0].sortOrder,
                learningObjective = fixtureMetadataList[0].learningObjective,
                shortNote = fixtureMetadataList[0].shortNote
            ),
            SyllabusNode(
                id = fixtureSubtopics[1].id,
                parentId = fixtureSubtopics[1].topicId,
                title = fixtureSubtopics[1].name,
                description = fixtureSubtopics[1].description,
                nodeType = SyllabusNodeType.SUBTOPIC,
                isActive = fixtureSubtopics[1].isActive,
                sortOrder = fixtureSubtopics[1].sortOrder,
                learningObjective = fixtureMetadataList[1].learningObjective,
                shortNote = fixtureMetadataList[1].shortNote
            )
        )
    }
}
