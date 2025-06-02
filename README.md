# E-Commerce Mobile App - Android

This repository contains the Android mobile application for the e-commerce platform.

## Project Overview

This Android application is part of the mobile strategy described in the [Mobile App Technical Specification](../confluence_samples/03_mobile_app_technical_spec.html).

## Related Jira Stories
- [MOB-345](../jira_stories/MOB-345.json): Implement offline shopping cart functionality

## Features

- Product browsing and search
- User account management
- Shopping cart functionality (online and offline)
- Order placement and tracking
- Personalized product recommendations
- Push notifications for order updates and promotions
- Barcode scanning for quick product lookup
- Store locator with integrated maps
- Wishlist management
- Product reviews and ratings

## Architecture

The app follows the MVVM (Model-View-ViewModel) architecture pattern with:
- Jetpack Compose for UI
- Kotlin Coroutines for asynchronous operations
- Kotlin Flow for reactive programming
- Hilt for dependency injection
- Room for local database
- Retrofit for API communication
- DataStore for preferences

## Technology Stack

- Kotlin 1.8+
- Android SDK 33+ (Android 13+)
- Jetpack Compose
- Android Architecture Components
- Material Design 3
- Retrofit for API communication
- Room for local database
- Hilt for dependency injection
- Coil for image loading
- Firebase for analytics and push notifications
- Google Maps SDK for store locator

## Getting Started

### Prerequisites
- Android Studio Hedgehog or later
- JDK 17
- Android SDK 33+

### Build and Run
```bash
# Clone the repository
git clone https://github.com/example/mobile-app-android.git

# Open in Android Studio
# Build and run on emulator or device
```

## Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/example/ecommerce/
│   │   │   ├── data/           # Data layer (repositories, data sources)
│   │   │   ├── di/             # Dependency injection modules
│   │   │   ├── domain/         # Domain layer (use cases, business logic)
│   │   │   ├── presentation/   # UI layer (screens, view models)
│   │   │   ├── util/           # Utility classes
│   │   │   └── MainActivity.kt # Entry point
│   │   ├── res/                # Resources (layouts, strings, etc.)
│   │   └── AndroidManifest.xml
│   ├── test/                   # Unit tests
│   └── androidTest/            # Instrumentation tests
├── build.gradle
└── proguard-rules.pro
```

## Testing

The app includes:
- Unit tests for view models and repositories
- UI tests with Espresso
- Integration tests for API communication
- End-to-end tests for critical user flows

## CI/CD

This repository is integrated with:
- GitHub Actions for CI
- Firebase App Distribution for beta testing
- Google Play Store for production releases

## Related Documentation

- [Mobile App Technical Specification](../confluence_samples/03_mobile_app_technical_spec.html)
- [API Documentation](../confluence_samples/08_api_documentation.html)
