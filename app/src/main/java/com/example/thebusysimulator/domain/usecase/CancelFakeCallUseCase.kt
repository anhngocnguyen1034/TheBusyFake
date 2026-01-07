package com.example.thebusysimulator.domain.usecase

import com.example.thebusysimulator.domain.repository.FakeCallRepository

/**
 * Use case to cancel a scheduled fake call
 */
class CancelFakeCallUseCase(
    private val repository: FakeCallRepository
) {
    suspend operator fun invoke(callId: String): Result<Unit> {
        return repository.cancelCall(callId)
    }
}







