package com.example.thebusysimulator.domain.usecase

import com.example.thebusysimulator.domain.repository.FakeCallRepository

/**
 * Use case to mark a fake call as completed
 */
class MarkCallAsCompletedUseCase(
    private val repository: FakeCallRepository
) {
    suspend operator fun invoke(callId: String): Result<Unit> {
        return repository.markCallAsCompleted(callId)
    }
}
