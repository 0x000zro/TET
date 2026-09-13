package com.example.domain.usecase.mocktest

import com.example.domain.model.mocktest.MockTestPerformance
import com.example.domain.repository.MockTestPerformanceRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case to observe or query historical mock test performances (Step 21).
 * Strictly isolated from PracticeAttempt calculations.
 */
class GetMockTestPerformanceHistoryUseCase(
    private val performanceRepository: MockTestPerformanceRepository
) {
    fun observeAll(): Flow<List<MockTestPerformance>> {
        return performanceRepository.observeAllPerformances()
    }

    suspend fun getAll(): List<MockTestPerformance> {
        return performanceRepository.getAllPerformances()
    }
}
