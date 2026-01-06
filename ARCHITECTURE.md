# Clean Architecture Structure

Dự án này được tổ chức theo mô hình **Clean Architecture** với 3 lớp chính:

## 📁 Cấu trúc Modules

### 1. **Domain Layer** (`:domain`)
- **Mục đích**: Chứa business logic thuần túy, độc lập với framework
- **Không phụ thuộc**: Android, UI, Database, Network
- **Cấu trúc**:
  - `model/` - Domain entities (business objects)
  - `repository/` - Repository interfaces
  - `usecase/` - Use cases (business logic)

### 2. **Data Layer** (`:data`)
- **Mục đích**: Xử lý dữ liệu từ các nguồn (API, Database, SharedPreferences, etc.)
- **Phụ thuộc**: Domain layer
- **Cấu trúc**:
  - `model/` - Data models (DTOs, Database entities)
  - `repository/` - Repository implementations
  - `datasource/` - Data sources (Remote, Local)
  - `mapper/` - Mappers để chuyển đổi giữa Data models và Domain entities

### 3. **Presentation Layer** (`:app`)
- **Mục đích**: UI và tương tác với người dùng
- **Phụ thuộc**: Domain layer, Data layer
- **Cấu trúc**:
  - `presentation/` - Activities, Fragments
  - `presentation/viewmodel/` - ViewModels
  - `presentation/ui/` - UI components (Compose screens, widgets)
  - `presentation/ui/theme/` - Theme, Colors, Typography

## 🔄 Luồng Dữ liệu

```
Presentation (UI) 
    ↓ (gọi)
Domain (Use Cases)
    ↓ (gọi)
Data (Repository Implementation)
    ↓ (gọi)
Data Sources (Remote/Local)
```

## 📋 Quy tắc Dependencies

1. **Domain** → Không phụ thuộc vào module nào
2. **Data** → Chỉ phụ thuộc vào Domain
3. **Presentation** → Phụ thuộc vào Domain và Data

## 🎯 Ví dụ Sử dụng

### Domain Layer
```kotlin
// domain/model/User.kt
data class User(val id: String, val name: String) : BaseEntity

// domain/repository/UserRepository.kt
interface UserRepository : BaseRepository {
    suspend fun getUser(id: String): User
}

// domain/usecase/GetUserUseCase.kt
class GetUserUseCase(private val repository: UserRepository) : BaseUseCase<String, User> {
    override suspend fun invoke(parameters: String): User {
        return repository.getUser(parameters)
    }
}
```

### Data Layer
```kotlin
// data/model/UserData.kt
data class UserData(val id: String, val name: String) : BaseDataModel

// data/mapper/UserMapper.kt
class UserMapper : Mapper<UserData, User> {
    override fun mapToEntity(data: UserData): User = User(data.id, data.name)
    override fun mapFromEntity(entity: User): UserData = UserData(entity.id, entity.name)
}

// data/repository/UserRepositoryImpl.kt
class UserRepositoryImpl(
    private val remoteDataSource: RemoteDataSource,
    private val mapper: UserMapper
) : BaseRepositoryImpl(), UserRepository {
    override suspend fun getUser(id: String): User {
        val userData = remoteDataSource.getUser(id)
        return mapper.mapToEntity(userData)
    }
}
```

### Presentation Layer
```kotlin
// app/presentation/viewmodel/UserViewModel.kt
class UserViewModel(
    private val getUserUseCase: GetUserUseCase
) : BaseViewModel() {
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()
    
    fun loadUser(id: String) {
        viewModelScope.launch {
            _user.value = getUserUseCase(id)
        }
    }
}
```

## 🚀 Bắt đầu Phát triển

1. Định nghĩa **Domain entities** và **Repository interfaces** trong `:domain`
2. Tạo **Use cases** trong `:domain` để xử lý business logic
3. Implement **Repository** trong `:data` với data sources
4. Tạo **ViewModels** và **UI** trong `:app` để hiển thị dữ liệu





