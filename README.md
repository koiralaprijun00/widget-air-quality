# Nepal Weather Widget

A weather application for Nepal that provides real-time weather and air quality information.

## Architecture

The application follows Clean Architecture principles with a clear separation of concerns:

### Package Structure

```
com.example.nepalweatherwidget/
├── core/                    # Core functionality and utilities
│   ├── di/                 # Dependency injection
│   ├── error/              # Error handling
│   ├── network/            # Network monitoring
│   └── utils/              # Utility classes
├── data/                   # Data layer
│   ├── local/             # Local data sources
│   ├── remote/            # Remote data sources
│   ├── repository/        # Repository implementations
│   └── model/             # Data models
├── domain/                # Domain layer
│   ├── model/            # Domain models
│   ├── repository/       # Repository interfaces
│   └── usecase/          # Use cases
├── presentation/         # Presentation layer
│   ├── ui/              # UI components
│   │   ├── dashboard/   # Dashboard screen
│   │   ├── map/        # Map screen
│   │   └── search/     # Search screen
│   └── viewmodel/      # ViewModels
├── widget/              # Widget components
└── worker/             # WorkManager workers
```

### Architecture Principles

1. **Clean Architecture**
   - Clear separation of concerns
   - Dependency rule: outer layers depend on inner layers
   - Domain layer is independent of other layers

2. **MVVM Pattern**
   - ViewModels handle UI logic
   - Views observe ViewModel state
   - Data binding for UI updates

3. **Repository Pattern**
   - Single source of truth
   - Abstracts data sources
   - Handles data operations

4. **Dependency Injection**
   - Hilt for dependency injection
   - Scoped dependencies
   - Easy testing

### Key Components

1. **Core Layer**
   - Common utilities
   - Error handling
   - Network monitoring
   - Location services

2. **Data Layer**
   - Remote data sources (API)
   - Local data sources (Room)
   - Repository implementations
   - Data models

3. **Domain Layer**
   - Business logic
   - Use cases
   - Repository interfaces
   - Domain models

4. **Presentation Layer**
   - UI components
   - ViewModels
   - UI state management
   - Navigation

5. **Widget Layer**
   - Weather widgets
   - Widget updates
   - Widget configuration

6. **Worker Layer**
   - Background tasks
   - Periodic updates
   - Data synchronization

## Development Guidelines

1. **Code Organization**
   - Follow package structure
   - Keep related code together
   - Use clear naming conventions

2. **Testing**
   - Unit tests for business logic
   - Integration tests for repositories
   - UI tests for critical flows

3. **Error Handling**
   - Use sealed classes for errors
   - Proper error propagation
   - User-friendly error messages

4. **Performance**
   - Efficient data loading
   - Background processing
   - Memory management

5. **Security**
   - Secure API key storage
   - Data encryption
   - Permission handling

## Setup

1. Clone the repository
2. Create a `secrets.properties` file in the root directory with the following content:
```properties
openweather_api_key=your_api_key_here
```
3. Replace `your_api_key_here` with your OpenWeather API key
4. Build and run the application

## Security Note

The `secrets.properties` file is gitignored to prevent accidental exposure of API keys. Never commit this file to version control.

## Features

- Current weather information
- Air quality data
- Location-based updates
- Widget support
- Material Design UI 