# Nimbus Weather App

## Overview
The Nimbus Weather App is an Android application that provides users with real-time weather information, forecasts for upcoming days, and additional weather-related details. The app fetches weather data from the OpenWeatherMap API based on the user's location.

## Features
- Displays current weather conditions (temperature, humidity, wind speed, pressure, UV index, visibility, etc.).
- Provides daily and weekly weather forecasts.
- Shows sunrise and sunset times with animations.
- Retrieves location-based weather data using GPS.
- Uses OpenWeatherMap API for real-time weather updates.
- Implements a smooth splash screen with transition.

## Important Data (Dependencies & API Keys)
```gradle
implementation "com.google.android.gms:play-services-location:21.2.0"
implementation "com.squareup.retrofit2:retrofit:2.9.0"
implementation "com.squareup.retrofit2:converter-gson:2.9.0"
```
```xml
<string name="weather_api_key">your_string_api_key</string>
```
Get API Key: [OpenWeatherMap API Keys](https://home.openweathermap.org/api_keys)

## Screenshots
![Nimbus Full Screenshot](https://github.com/swarup62/Nimbus/blob/dd9843d24780d53e743bc6fd01a62e669677725e/nimbus_full_screenshot.jpg?raw=true)

## Project Structure

### 1. Manifest File (`AndroidManifest.xml`)
- Describes essential app information for the Android system.
- Declares required permissions for location access and internet usage.

**Key elements:**
- `<uses-permission>`: Declares required permissions like `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`, and `INTERNET`.
- `<application>`: Contains metadata about the app, including icons, labels, themes, and activities.
- `<activity>`: Declares activities such as `Splash`, `Home`, and `AboutUs`.
- `<intent-filter>`: Specifies the entry point (`Splash` activity).
- `<meta-data>`: Includes metadata such as the Google Maps API key.

### 2. Layout Files (`res/layout/`)
- **`splash.xml`**: Splash screen layout with app logo and name.
- **`home.xml`**: Main screen layout displaying weather data using `TextView`, `ImageView`, `CardView`, and `ConstraintLayout`.
- **`aboutus.xml`**: Displays developer information and app details.

### 3. Java Source Files (`src/main/java/`)
#### **`Splash.java` (Splash Activity)**
- Displays splash screen upon launch.
- Uses a `Handler` to navigate to `Home.java` after a 2-second delay.

#### **`Home.java` (Main Activity)**
- Manages UI elements and user interactions.
- Uses `FusedLocationProviderClient` to fetch the user's location.
- Converts coordinates into city names using `Geocoder`.
- Fetches weather data using `Retrofit` and updates UI.
- Displays forecast summaries and weather probabilities.
- Handles location permission requests.

#### **`AboutUs.java` (About Us Activity)**
- Displays information about the app and developers.

### 4. Resources (`res/values/`)
- **`strings.xml`**: Defines reusable strings such as app name, API keys, and UI labels.
```xml
<string name="weather_api_key">your_string_api_key</string>
```
Get API Key: [OpenWeatherMap API Keys](https://home.openweathermap.org/api_keys)

### 5. Dependencies (Gradle Build Files)
Add the following dependencies in `build.gradle`:
```gradle
implementation "com.google.android.gms:play-services-location:21.2.0"
implementation "com.squareup.retrofit2:retrofit:2.9.0"
implementation "com.squareup.retrofit2:converter-gson:2.9.0"
```

## Installation & Setup
1. Clone the repository:
   ```sh
   git clone https://github.com/your-repo/nimbus-weather-app.git
   ```
2. Open the project in Android Studio.
3. Add your OpenWeatherMap API key in `strings.xml`.
4. Build and run the application on an emulator or physical device.

## License
This project is licensed under the MIT License.

## Contributing
Feel free to contribute to this project by submitting issues or pull requests. Happy coding! 🚀

