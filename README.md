# Smart Daily Water Intake Tracker 

## Overview

The **Smart Daily Water Intake Tracker** is an Android application designed to help users maintain healthy hydration habits by tracking daily water consumption. The app offers intelligent reminders, weather and activity-based recommendations, and an easy-to-use interface for logging intake. It integrates modern Android development tools, including Jetpack Compose, Room Database, Healthconnect API, OpenWeather API, and Firebase.

## Technologies Used

- **Android Studio** (Kotlin + Jetpack Compose)
- **Room Database** – Local storage for hydration entries.
- **Healthconnect API** – Fetches step count data.
- **OpenWeather API** – Provides real-time temperature and humidity data.
- **Firebase** – Used for optional cloud backups and user authentication.
- **Jetpack Libraries** – Navigation, Lifecycle, ViewModel, LiveData, etc.

SmartWaterIntake/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/smartwaterintake/
│   │   │   │   ├── ui/                         # Jetpack Compose UI screens and components
│   │   │   │   │   ├── HomeScreen.kt           # Main dashboard showing water intake, steps, and reminders
│   │   │   │   │   ├── DiaryScreen.kt          # Displays past hydration entries and trend graph
│   │   │   │   │   ├── ProfileScreen.kt        # Allows users to view and edit personal profile details
│   │   │   │   │   ├── WelcomeScreen.kt        # Optional welcome/introduction screen
│   │   │   │   │   └── components/             # Reusable UI components (buttons, dialogs, charts)
│   │   │   │   ├── data/                       # Local Room database setup
│   │   │   │   │   ├── Cup.kt                  # Data class representing a single water intake entry
│   │   │   │   │   ├── CupDao.kt               # DAO interface defining database operations for `Cup`
│   │   │   │   │   ├── CupDatabase.kt          # Abstract RoomDatabase managing Cup table
│   │   │   │   ├── api/                        # API integrations
│   │   │   │   │   ├── WeatherApi.kt           # Retrofit interface for OpenWeatherMap API
│   │   │   │   │   └── GoogleFitService.kt     # Class for accessing Google Fit step data
│   │   │   │   ├── model/                      # Data and domain models
│   │   │   │   │   ├── UserProfile.kt          # User profile data (weight, height, name)
│   │   │   │   │   ├── WeatherData.kt          # Models temperature and humidity from OpenWeather
│   │   │   │   │   └── HydrationGoal.kt        # Represents calculated water intake goals
│   │   │   │   ├── viewmodel/                  # ViewModel classes for state handling
│   │   │   │   │   ├── HomeViewModel.kt        # Manages state and logic for Home screen
│   │   │   │   │   ├── DiaryViewModel.kt       # Manages past entries and graph logic
│   │   │   │   │   ├── ProfileViewModel.kt     # Manages user input and hydration goal logic
│   │   │   │   │   └── ReminderViewModel.kt    # Handles notification scheduling logic
│   │   │   │   └── util/                       # Utility and helper functions
│   │   │   │       ├── NotificationUtil.kt     # Utility class to send hydration reminders
│   │   │   │       └── TimeUtils.kt            # Helpers for time/date formatting
│   │   │   ├── res/
│   │   │   │   ├── layout/                     # XML layout files (if any non-Compose screens exist)
│   │   │   │   ├── values/                     # Resource values: strings, colors, themes
│   │   │   │   │   ├── strings.xml             # App text strings
│   │   │   │   │   ├── colors.xml              # App colour definitions
│   │   │   │   │   └── themes.xml              # Material Design themes and styles
│   │   │   │   ├── drawable/                   # Icons and vector assets
│   │   │   │   │   └── ic_launcher.xml         # App launcher icon vector
│   │   │   └── AndroidManifest.xml             # App permissions, services, and entry points
├── build.gradle.kts                            # Root Gradle build configuration
├── settings.gradle.kts                         # Project settings and module declarations
└── README.md                                   # Project overview and documentation

