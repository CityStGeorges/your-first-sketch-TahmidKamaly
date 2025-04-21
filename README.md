# Smart Daily Water Intake Tracker 🚰

## Overview

The **Smart Daily Water Intake Tracker** is an Android application designed to help users maintain healthy hydration habits by tracking daily water consumption. The app offers intelligent reminders, weather and activity-based recommendations, and an easy-to-use interface for logging intake. It integrates modern Android development tools, including Jetpack Compose, Room Database, Healthconnect API, OpenWeather API, and Firebase.

## Features

- **Quick-Add Logging**: Add predefined water quantities (e.g., 250ml, 500ml) with a single tap.
- **Hydration Diary**: Visualise weekly/monthly hydration trends using charts.
- **Smart Reminders**: Automatically reminds users to drink water based on inactivity or every 1000 steps.
- **Weather-Based Adjustments**: Hydration goals adapt to local temperature and humidity via OpenWeather API.
- **Step-Based Adjustments**: Google Fit integration increases water goal based on daily step count.
- **Profile Tab**: Users can view and edit personal metrics such as height and weight to personalise hydration goals.
- **Local & Cloud Storage**: Data is saved using Room (local DB) and optionally backed up to Firebase.
- **Dark Mode Toggle**: Enables switching between light and dark UI themes.

## Technologies Used

- **Android Studio** (Kotlin + Jetpack Compose)
- **Room Database** – Local storage for hydration entries.
- **Healthconnect API** – Fetches step count data.
- **OpenWeather API** – Provides real-time temperature and humidity data.
- **Firebase** – Used for optional cloud backups and user authentication.
- **Jetpack Libraries** – Navigation, Lifecycle, ViewModel, LiveData, etc.

## Project Structure

```text
SmartWaterIntake/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/smartwaterintake/
│   │   │   │   ├── ui/               # Jetpack Compose screens
│   │   │   │   ├── data/             # Room database classes
│   │   │   │   ├── api/              # Google Fit & Weather API integration
│   │   │   │   ├── model/            # Data models
│   │   │   │   ├── viewmodel/        # ViewModels
│   │   │   │   └── util/             # Utility/helper functions
│   │   │   ├── res/                  # Layouts, values, icons
│   │   │   └── AndroidManifest.xml   # App manifest
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
