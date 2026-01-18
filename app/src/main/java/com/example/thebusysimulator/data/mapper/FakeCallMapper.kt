package com.example.thebusysimulator.data.mapper

import com.example.thebusysimulator.data.model.FakeCallData
import com.example.thebusysimulator.data.model.FakeCallEntity
import com.example.thebusysimulator.domain.model.FakeCall
import java.util.Date

/**
 * Mapper to convert between FakeCallData, FakeCallEntity and FakeCall
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
    
    fun mapFromRoomEntity(entity: FakeCallEntity): FakeCall {
        return FakeCall(
            id = entity.id,
            callerName = entity.callerName,
            callerNumber = entity.callerNumber,
            scheduledTime = Date(entity.scheduledTime),
            isCompleted = entity.isCompleted,
            isVideoCall = entity.isVideoCall
        )
    }
    
    fun mapToRoomEntity(call: FakeCall): FakeCallEntity {
        return FakeCallEntity(
            id = call.id,
            callerName = call.callerName,
            callerNumber = call.callerNumber,
            scheduledTime = call.scheduledTime.time,
            isCompleted = call.isCompleted,
            isVideoCall = call.isVideoCall
        )
    }
    
    fun mapDataToRoomEntity(data: FakeCallData): FakeCallEntity {
        return FakeCallEntity(
            id = data.id,
            callerName = data.callerName,
            callerNumber = data.callerNumber,
            scheduledTime = data.scheduledTime,
            isCompleted = data.isCompleted,
            isVideoCall = data.isVideoCall
        )
    }
}







