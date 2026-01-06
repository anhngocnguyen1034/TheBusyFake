package com.example.thebusysimulator.data.mapper

import com.example.thebusysimulator.data.model.FakeCallData
import com.example.thebusysimulator.domain.model.FakeCall
import java.util.Date

/**
 * Mapper to convert between FakeCallData and FakeCall
 */
class FakeCallMapper {
    fun mapFromEntity(entity: FakeCall): FakeCallData {
        return FakeCallData(
            id = entity.id,
            callerName = entity.callerName,
            callerNumber = entity.callerNumber,
            scheduledTime = entity.scheduledTime.time,
            isCompleted = entity.isCompleted,
            isVideoCall = entity.isVideoCall
        )
    }

    fun mapToEntity(data: FakeCallData): FakeCall {
        return FakeCall(
            id = data.id,
            callerName = data.callerName,
            callerNumber = data.callerNumber,
            scheduledTime = Date(data.scheduledTime),
            isCompleted = data.isCompleted,
            isVideoCall = data.isVideoCall
        )
    }
}






