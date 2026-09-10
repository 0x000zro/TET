package com.example.domain.model

/**
 * Pure domain extension functions converting hierarchy domain entities to generic [SyllabusNode]s.
 */
fun Exam.toSyllabusNode(metadata: SyllabusMetadata? = null): SyllabusNode {
    return SyllabusNode(
        id = this.id,
        parentId = null,
        title = this.name,
        description = this.description,
        nodeType = SyllabusNodeType.EXAM,
        isActive = this.isActive,
        sortOrder = this.sortOrder,
        learningObjective = metadata?.learningObjective.orEmpty(),
        shortNote = metadata?.shortNote.orEmpty()
    )
}

fun Paper.toSyllabusNode(metadata: SyllabusMetadata? = null): SyllabusNode {
    return SyllabusNode(
        id = this.id,
        parentId = this.examId,
        title = this.name,
        description = this.description,
        nodeType = SyllabusNodeType.PAPER,
        isActive = this.isActive,
        sortOrder = this.sortOrder,
        learningObjective = metadata?.learningObjective.orEmpty(),
        shortNote = metadata?.shortNote.orEmpty()
    )
}

fun Subject.toSyllabusNode(metadata: SyllabusMetadata? = null): SyllabusNode {
    return SyllabusNode(
        id = this.id,
        parentId = this.paperId,
        title = this.name,
        description = this.description,
        nodeType = SyllabusNodeType.SUBJECT,
        isActive = this.isActive,
        sortOrder = this.sortOrder,
        learningObjective = metadata?.learningObjective.orEmpty(),
        shortNote = metadata?.shortNote.orEmpty()
    )
}

fun Topic.toSyllabusNode(metadata: SyllabusMetadata? = null): SyllabusNode {
    return SyllabusNode(
        id = this.id,
        parentId = this.subjectId,
        title = this.name,
        description = this.description,
        nodeType = SyllabusNodeType.TOPIC,
        isActive = this.isActive,
        sortOrder = this.sortOrder,
        learningObjective = metadata?.learningObjective.orEmpty(),
        shortNote = metadata?.shortNote.orEmpty()
    )
}

fun Subtopic.toSyllabusNode(metadata: SyllabusMetadata? = null): SyllabusNode {
    return SyllabusNode(
        id = this.id,
        parentId = this.topicId,
        title = this.name,
        description = this.description,
        nodeType = SyllabusNodeType.SUBTOPIC,
        isActive = this.isActive,
        sortOrder = this.sortOrder,
        learningObjective = metadata?.learningObjective.orEmpty(),
        shortNote = metadata?.shortNote.orEmpty()
    )
}
