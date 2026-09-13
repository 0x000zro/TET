package com.example.data.repository

import com.example.domain.model.mocktest.MockTestPerformance
import com.example.domain.repository.MockTestPerformanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Thread-safe in-memory implementation of [MockTestPerformanceRepository] (Step 21).
 * Avoids premature Room schema migration while providing complete repository abstraction.
 */
class InMemoryMockTestPerformanceRepository : MockTestPerformanceRepository {
    private val records = CopyOnWriteArrayList<MockTestPerformance>()
    private val recordsFlow = MutableStateFlow<List<MockTestPerformance>>(emptyList())

    override suspend fun recordPerformance(performance: MockTestPerformance) {
        records.add(0, performance)
        recordsFlow.value = records.toList()
    }

    override suspend fun getAllPerformances(): List<MockTestPerformance> {
        return records.toList()
    }

    override fun observeAllPerformances(): Flow<List<MockTestPerformance>> {
        return recordsFlow.asStateFlow()
    }

    override suspend fun getPerformancesByExam(examId: String): List<MockTestPerformance> {
        return records.filter { it.examId == examId }
    }

    override suspend fun getPerformancesByPaper(paperId: String): List<MockTestPerformance> {
        return records.filter { it.paperId == paperId }
    }

    override suspend fun clearPerformances() {
        records.clear()
        recordsFlow.value = emptyList()
    }
}
