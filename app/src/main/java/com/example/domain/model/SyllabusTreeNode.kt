package com.example.domain.model

/**
 * Pure domain model representing an entire syllabus branch or tree:
 * a syllabus node together with its directly resolved child nodes.
 */
data class SyllabusTreeNode(
    val node: SyllabusNode,
    val children: List<SyllabusTreeNode> = emptyList()
) {
    /**
     * Total count of all descendants recursively under this node.
     */
    val totalDescendantsCount: Int
        get() = children.sumOf { 1 + it.totalDescendantsCount }

    /**
     * True if this node has no children.
     */
    val isLeaf: Boolean
        get() = children.isEmpty()
}
