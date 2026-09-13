package com.example.domain.repository

import com.example.domain.model.mocktest.MockTestPerformance
import kotlinx.coroutines.flow.Flow

/**
 * Pure repository abstraction for completed Mock Test performance records (Step 21).
 * Strictly segregated from PracticeAttempt and Practice performance tracking.
 */
interface MockTestPerformanceRepository {
    suspend fun recordPerformance(performance: MockTestPerformance)
    suspend fun getAllPerformances(): List<MockTestPerformance>
    fun observeAllPerformances(): Flow<List<MockTestPerformance>>
    suspend fun getPerformancesByExam(examId: String): List<MockTestPerformance>
    suspend fun getPerformancesByPaper(paperId: String): List<MockTestPerformance>
    suspend fun clearPerformances()
}
