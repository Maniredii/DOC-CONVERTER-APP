# PDF Converter Pro

A modern Android application for converting documents between PDF, DOCX, and other formats. Built with Material Design 3 and modern Android architecture components.

## Features

### 🔄 Document Conversion

* **PDF to DOCX**: Convert PDF files to editable Word documents
* **DOCX to PDF**: Convert Word documents to PDF format
* **Any to PDF**: Convert various formats (TXT, RTF, XLSX, etc.) to PDF

### 🎨 Modern UI/UX

* Material Design 3 interface
* Intuitive card-based layout
* Progress tracking during conversion
* File selection and management
* Permission handling with user guidance

### 🏗️ Architecture

* MVVM architecture pattern
* ViewBinding for UI interactions
* LiveData for reactive UI updates
* Coroutines for asynchronous operations
* Room database for conversion history (planned)

## Screenshots

_Screenshots will be added here once the app is built and running_

## Technical Stack

### Core Technologies

* **Language**: Kotlin
* **Minimum SDK**: API 26 (Android 8.0)
* **Target SDK**: API 35 (Android 15)
* **Build System**: Gradle with Kotlin DSL

### Libraries & Dependencies

* **UI Components**: Material Design 3, AppCompat
* **PDF Processing**: iText7
* **Office Documents**: Apache POI
* **Architecture**: ViewModel, LiveData, Coroutines
* **File Handling**: DocumentFile, FileProvider

### Key Features

* **ViewBinding**: Type-safe view access
* **Coroutines**: Asynchronous file processing
* **File Provider**: Secure file sharing
* **Permission Handling**: Runtime permissions with user guidance

## Project Structure

```
app/src/main/java/fm/mrc/pdftodocxconverter/
├── MainActivity.kt                 # Main entry point
├── ConversionActivity.kt           # File conversion screen
├── converter/
│   ├── DocumentConverter.kt        # Core conversion logic
│   └── ConversionType.kt          # Conversion type enum
├── viewmodel/
│   └── ConversionViewModel.kt      # Business logic & state management
└── res/
    ├── layout/                     # UI layouts
    ├── drawable/                   # Vector icons
    ├── values/                     # Colors, strings, themes
    └── xml/                        # File provider paths

```

## Setup Instructions

### Prerequisites

* Android Studio Hedgehog or later
* Android SDK API 26+
* Java 11 or later

### Installation Steps

1. **Clone the repository**  
   ```bash
   git clone https://github.com/Maniredii/DOC-CONVERTER-APP.git
   cd DOC-CONVERTER-APP
   ```
2. **Open in Android Studio**  
   * Launch Android Studio  
   * Open the project folder  
   * Wait for Gradle sync to complete
3. **Build and Run**  
   * Connect an Android device or start an emulator  
   * Click the "Run" button (▶️) in Android Studio  
   * Select your target device and click "OK"

### Build Configuration

The app uses the following key configurations:

```kotlin
android {
    compileSdk = 35
    minSdk = 26
    targetSdk = 35
    
    buildFeatures {
        viewBinding = true
    }
}
```

## Usage Guide

### 1. Launch the App

* Open the app from your device's app drawer
* Grant file access permissions when prompted

### 2. Select Conversion Type

* **PDF to DOCX**: Convert PDF files to Word documents
* **DOCX to PDF**: Convert Word documents to PDF
* **Any to PDF**: Convert various formats (TXT, RTF, XLSX, etc.) to PDF

### 3. Choose File

* Tap the "Convert" button for your desired conversion
* Select a file from your device storage
* The app will analyze the file format

### 4. Monitor Progress

* View real-time conversion progress
* Cancel conversion if needed
* Wait for completion notification

### 5. Access Converted File

* Tap "Open File" to view the converted document
* Files are saved in the app's external files directory
* Use your device's file manager to access converted files

## Development Roadmap

### Phase 1: Core Functionality ✅

* Basic UI structure
* File selection and permissions
* PDF to DOCX conversion
* DOCX to PDF conversion
* Progress tracking

### Phase 2: Enhanced Features 🚧

* Advanced PDF creation with formatting
* Support for more input formats (XLSX, RTF, etc.)
* Batch conversion capabilities
* Conversion history and favorites

### Phase 3: Advanced Features 📋

* Cloud storage integration
* OCR text extraction
* Document editing capabilities
* Advanced formatting options

### Phase 4: Polish & Optimization 📋

* Performance optimization
* Accessibility improvements
* Multi-language support
* Dark theme support

## Contributing

We welcome contributions! Here's how you can help:

### Development Setup

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Make your changes and commit: `git commit -m 'Add amazing feature'`
4. Push to the branch: `git push origin feature/amazing-feature`
5. Open a Pull Request

### Code Style

* Follow Kotlin coding conventions
* Use meaningful variable and function names
* Add comments for complex logic
* Include unit tests for new features

### Testing

* Test on multiple Android versions
* Verify file conversion accuracy
* Check UI on different screen sizes
* Ensure proper error handling

## Troubleshooting

### Common Issues

**Build Errors**

* Ensure you have the correct Java version (11+)
* Clean and rebuild the project
* Sync Gradle files

**Permission Issues**

* Grant storage permissions in device settings
* Check if the app has necessary permissions
* Restart the app after granting permissions

**Conversion Failures**

* Verify input file format is supported
* Check available storage space
* Ensure file is not corrupted

### Debug Mode

Enable debug logging by setting:

```kotlin
Log.d(TAG, "Debug message")
```

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

* **iText7**: PDF processing capabilities
* **Apache POI**: Office document handling
* **Material Design**: UI/UX guidelines
* **Android Jetpack**: Architecture components

## Support

For support and questions:

* Create an issue on [GitHub](https://github.com/Maniredii/DOC-CONVERTER-APP)
* Check the troubleshooting section
* Review the code documentation

---

**Note**: This is a development version. Some features may be incomplete or subject to change.
