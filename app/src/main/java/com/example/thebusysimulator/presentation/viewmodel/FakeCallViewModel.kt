package com.example.thebusysimulator.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thebusysimulator.data.datasource.FakeCallSettingsDataSource
import com.example.thebusysimulator.domain.model.FakeCall
import com.example.thebusysimulator.domain.usecase.CancelFakeCallUseCase
import com.example.thebusysimulator.domain.usecase.GetScheduledCallsUseCase
import com.example.thebusysimulator.domain.usecase.MarkCallAsCompletedUseCase
import com.example.thebusysimulator.domain.usecase.ScheduleFakeCallUseCase
import com.example.thebusysimulator.domain.util.CallScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

data class FakeCallUiState(
    val scheduledCalls: List<FakeCall> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isScheduling: Boolean = false,
    val vibrationEnabled: Boolean = true,
    val flashEnabled: Boolean = false
)

class FakeCallViewModel(
    private val scheduleFakeCallUseCase: ScheduleFakeCallUseCase,
    private val cancelFakeCallUseCase: CancelFakeCallUseCase,
    private val getScheduledCallsUseCase: GetScheduledCallsUseCase,
    private val markCallAsCompletedUseCase: MarkCallAsCompletedUseCase,
    private val callScheduler: CallScheduler,
    private val settingsDataSource: FakeCallSettingsDataSource
) : ViewModel() {

    private val _uiState = MutableStateFlow(FakeCallUiState())
    val uiState: StateFlow<FakeCallUiState> = _uiState.asStateFlow()

    init {
        loadScheduledCalls()
        observeSettings() // Start observing immediately - it will load initial values
    }

    private fun observeSettings() {
        combine(
            settingsDataSource.vibrationEnabled,
            settingsDataSource.flashEnabled
        ) { vibration, flash ->
            vibration to flash
        }.onEach { (vibration, flash) ->
            _uiState.value = _uiState.value.copy(
                vibrationEnabled = vibration,
                flashEnabled = flash
            )
        }.launchIn(viewModelScope)
    }

    fun scheduleCall(
        callerName: String,
        callerNumber: String,
        scheduledTime: Date
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isScheduling = true, errorMessage = null)
            
            val fakeCall = FakeCall(
                id = UUID.randomUUID().toString(),
                callerName = callerName,
                callerNumber = callerNumber,
                scheduledTime = scheduledTime,
                isVideoCall = false
            )

            scheduleFakeCallUseCase.invoke(fakeCall).fold(
                onSuccess = {
                    callScheduler.schedule(fakeCall)
                    // Mark call as completed ngay sau khi schedule để hiển thị trong lịch sử
                    markCallAsCompletedUseCase.invoke(fakeCall.id).fold(
                        onSuccess = {
                            _uiState.value = _uiState.value.copy(isScheduling = false)
                            loadScheduledCalls()
                        },
                        onFailure = { markException ->
                            // Nếu mark failed, vẫn coi như schedule thành công
                            android.util.Log.w("FakeCallViewModel", "Failed to mark call as completed: ${markException.message}")
                            _uiState.value = _uiState.value.copy(isScheduling = false)
                            loadScheduledCalls()
                        }
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isScheduling = false,
                        errorMessage = exception.message ?: "Failed to schedule call"
                    )
                }
            )
        }
    }

    fun cancelCall(callId: String) {
        viewModelScope.launch {
            cancelFakeCallUseCase.invoke(callId).fold(
                onSuccess = {
                    callScheduler.cancel(callId)
                    loadScheduledCalls()
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = exception.message ?: "Failed to cancel call"
                    )
                }
            )
        }
    }

    private fun loadScheduledCalls() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            getScheduledCallsUseCase.invoke().fold(
                onSuccess = { calls ->
                    _uiState.value = _uiState.value.copy(
                        scheduledCalls = calls,
                        isLoading = false
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Failed to load calls"
                    )
                }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    fun testCallNow(callerName: String, callerNumber: String) {
        // Test fake call immediately without scheduling
        callScheduler.testCallNow(callerName, callerNumber, false)
    }

    fun setVibrationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataSource.setVibrationEnabled(enabled)
            // State will be updated automatically by observeSettings()
        }
    }

    fun setFlashEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataSource.setFlashEnabled(enabled)
            // State will be updated automatically by observeSettings()
        }
    }
}

