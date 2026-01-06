package com.example.thebusysimulator.domain.usecase

import com.example.thebusysimulator.domain.model.FakeCall
import com.example.thebusysimulator.domain.repository.FakeCallRepository

/**
 * Use case to get all scheduled fake calls
 */
class GetScheduledCallsUseCase(
    private val repository: FakeCallRepository
) {
    suspend operator fun invoke(): Result<List<FakeCall>> {
        return repository.getScheduledCalls()
    }
}






