# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is the BlueSnap Android SDK - a payment processing library that enables Android apps to accept credit card, Google Pay, and PayPal payments. The SDK consists of two main modules:

- **bluesnap-android/**: The core SDK library module
- **DemoApp/**: Demo application showing SDK integration examples

## Build and Development Commands

### Building the Project
JAVA_HOME might be missing as environment variable but can be set to the android studio included jdk usually in  ~/Downloads/android-studio/jbr
```bash
# Build the entire project
./gradlew build

# Build only the SDK library
./gradlew :bluesnap-android:build

# Build only the Demo app
./gradlew :DemoApp:build
```

### Running Tests

```bash
# Run all unit tests
./gradlew test

# Run unit tests for SDK only
./gradlew :bluesnap-android:test

# Run instrumentation tests (requires connected Android device/emulator)
./gradlew connectedAndroidTest

# Prepare device for UI tests (disables Gboard features that interfere with Espresso)
./gradlew prepareUiTestDevice
```

### Code Quality

```bash
# Run checkstyle (configured for bluesnap-android module)
./gradlew checkstyle

# Run lint checks
./gradlew lint
```

## Architecture

### Core SDK Structure (`bluesnap-android/`)

- **models/**: Data models for payments, billing, shipping, and SDK configuration
  - `CreditCard.kt`, `EcpAchDetails.kt` - Payment method models
  - `BillingContactInfo.java`, `ShippingContactInfo.java` - Contact information
  - `SdkRequest.kt`, `SdkResult.java` - Request/response models
  - `PurchaseDetails.java` - Purchase transaction details

- **services/**: Core business logic and API communication
  - `BlueSnapService.kt` - Main SDK service and entry point
  - `BlueSnapAPI.kt` - HTTP API communication layer
  - `GooglePayService.kt` - Google Pay integration
  - `CardinalManager.java` - 3D Secure authentication via Cardinal
  - `KountService.kt` - Fraud prevention integration

- **views/**: UI components for payment forms
  - `activities/` - Main payment flow activities
  - `fragments/` - Payment form fragments
  - `components/` - Reusable UI components

- **http/**: HTTP operation handling and threading
  - `HTTPOperationController.java` - Network request management
  - `AppExecutors.java` - Threading utilities

### Demo App Structure (`DemoApp/`)

- **DemoMainActivity.java**: Main demo activity showing different payment flows
- **DemoTransactions.java**: Transaction examples and server communication
- **androidTest/**: Comprehensive UI test suite using Espresso
  - Tests cover checkout flows, payment methods, and various configurations

## Key Configuration

### SDK Versions
Values are set in the root project build.gradle file which is used in the demoapp.
Most of This project was migrated from java to kotlin
Some implementations are old since this project started with android 10 and not everything was re-written to support new Android APIs.



### Development Setup

1. **API Credentials**: exist in local uncommited file `local.gradle` and add your BlueSnap sandbox API credentials
2. **Environment Variables**: Alternatively, set `BS_API_USER`, `BS_API_PASSWORD`, `BS_PAYPAL_EMAIL`, `BS_PAYPAL_PASSWORD`

### Testing

- **Unit Tests**: Located in `src/test/` directories, use JUnit and Mockito
- **Integration Tests**: Located in `src/androidTest/`, require BlueSnap API credentials
- **UI Tests**: Comprehensive Espresso test suite in DemoApp covering all payment flows
- **Device Preparation**: UI tests require running `prepareUiTestDevice` task to disable interfering Gboard features
#### known test issues
- UI tests can fail due to various reasons such as keyboard hiding some input fields. emulator slowness, connectivity issues.
- Cardinal provides a list of test credit cards in their documentation online
- the SanitySuite.java contains the most important ui tests that should be stable. some other ui tests are flaky,s
- 
### External Dependencies

- **Cardinal Commerce SDK**: For 3D Secure authentication
- **Google Play Services**: For Google Pay integration  
- **Kount SDK**: For fraud prevention (included as JAR in libs/)
- **Material Design Components**: For UI styling

## Common Development Tasks

### Adding New Payment Methods
1. Create model class in `models/` package
2. Add service integration in `services/` package  
3. Create UI components in `views/` package
4. Update `SdkRequest` and `SdkResult` classes
5. Add comprehensive tests

### Modifying UI Components
- UI resources organized by type in `res/layouts/` subdirectories
- Card images in `res-cards/`, country flags in `res-flags/`
- Localization files in `values-xx/` directories for multiple languages

### API Changes
- Update models in `models/` package
- Modify `BlueSnapAPI.kt` for endpoint changes
- Update JSON parsing in `utils/JsonParser.java`
- Ensure backward compatibility in `SdkRequest`/`SdkResult`