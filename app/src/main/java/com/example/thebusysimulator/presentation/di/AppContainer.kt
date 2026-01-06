package com.example.thebusysimulator.presentation.di

import android.content.Context
import com.example.thebusysimulator.data.datasource.FakeCallSettingsDataSource
import com.example.thebusysimulator.data.datasource.LocalFakeCallDataSource
import com.example.thebusysimulator.data.datasource.LocalFakeCallDataSourceImpl
import com.example.thebusysimulator.data.database.AppDatabase
import com.example.thebusysimulator.data.mapper.FakeCallMapper
import com.example.thebusysimulator.data.repository.FakeCallRepositoryImpl
import com.example.thebusysimulator.data.repository.MessageRepository
import com.example.thebusysimulator.domain.repository.FakeCallRepository
import com.example.thebusysimulator.domain.usecase.CancelFakeCallUseCase
import com.example.thebusysimulator.domain.usecase.GetScheduledCallsUseCase
import com.example.thebusysimulator.domain.usecase.ScheduleFakeCallUseCase
import com.example.thebusysimulator.domain.util.CallScheduler
import com.example.thebusysimulator.presentation.util.AlarmSchedulerImpl
import com.example.thebusysimulator.presentation.viewmodel.FakeCallViewModel
import com.example.thebusysimulator.presentation.viewmodel.FakeMessageViewModel
import com.example.thebusysimulator.presentation.viewmodel.MessageViewModel

/**
 * Simple dependency injection container
 * In production, use Hilt or Koin
 */
object AppContainer {
    private var context: Context? = null
    private var database: AppDatabase? = null
    
    fun init(context: Context) {
        this.context = context.applicationContext
        database = AppDatabase.getDatabase(context.applicationContext)
    }
    // Data Source
    private val localDataSource: LocalFakeCallDataSource = LocalFakeCallDataSourceImpl()
    
    // Mapper
    private val fakeCallMapper: FakeCallMapper = FakeCallMapper()
    
    // Repository
    private val fakeCallRepository: FakeCallRepository = FakeCallRepositoryImpl(
        localDataSource = localDataSource,
        mapper = fakeCallMapper
    )
    
    // Message Repository
    private val messageRepository: MessageRepository by lazy {
        requireNotNull(database) { "AppContainer must be initialized with context" }
        MessageRepository(
            messageDao = database!!.messageDao(),
            chatMessageDao = database!!.chatMessageDao()
        )
    }
    
    // Use Cases
    val scheduleFakeCallUseCase: ScheduleFakeCallUseCase = ScheduleFakeCallUseCase(fakeCallRepository)
    val cancelFakeCallUseCase: CancelFakeCallUseCase = CancelFakeCallUseCase(fakeCallRepository)
    val getScheduledCallsUseCase: GetScheduledCallsUseCase = GetScheduledCallsUseCase(fakeCallRepository)
    
    // Settings DataSource
    private val settingsDataSource: FakeCallSettingsDataSource by lazy {
        requireNotNull(context) { "AppContainer must be initialized with context" }
        FakeCallSettingsDataSource(context!!)
    }
    
    // Scheduler
    private val callScheduler: CallScheduler by lazy {
        requireNotNull(context) { "AppContainer must be initialized with context" }
        AlarmSchedulerImpl(context!!)
    }
    
    // ViewModels
    fun createFakeCallViewModel(): FakeCallViewModel {
        return FakeCallViewModel(
            scheduleFakeCallUseCase = scheduleFakeCallUseCase,
            cancelFakeCallUseCase = cancelFakeCallUseCase,
            getScheduledCallsUseCase = getScheduledCallsUseCase,
            callScheduler = callScheduler,
            settingsDataSource = settingsDataSource
        )
    }
    
    fun createMessageViewModel(): MessageViewModel {
        requireNotNull(context) { "AppContainer must be initialized with context" }
        return MessageViewModel(
            messageRepository = messageRepository,
            context = context
        )
    }
    
    fun createFakeMessageViewModel(): FakeMessageViewModel {
        requireNotNull(context) { "AppContainer must be initialized with context" }
        return FakeMessageViewModel(context = context!!)
    }
}

