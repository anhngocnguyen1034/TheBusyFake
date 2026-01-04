package com.example.thebusysimulator.domain.usecase

import com.example.thebusysimulator.domain.model.FakeCall
import com.example.thebusysimulator.domain.repository.FakeCallRepository

/**
 * Use case to schedule a fake call
 */
class ScheduleFakeCallUseCase(
    private val repository: FakeCallRepository
) {
    suspend operator fun invoke(fakeCall: FakeCall): Result<FakeCall> {
        return repository.scheduleCall(fakeCall)
    }
}


