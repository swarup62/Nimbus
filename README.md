Nimbus Weather App

Overview
The Nimbus Weather App is an Android application that provides users with real-time weather information, forecasts for upcoming days, and additional weather-related details. The app fetches weather data from the OpenWeatherMap API based on the user's location.

Features
•	Displays current weather conditions (temperature, humidity, wind speed, pressure, UV index, visibility, etc.).
•	Provides daily and weekly weather forecasts.
•	Shows sunrise and sunset times with animations.
•	Retrieves location-based weather data using GPS.
•	Uses OpenWeatherMap API for real-time weather updates.
•	Implements a smooth splash screen with transition.

*Important Datas (Dependencies & API Keys)*
        i. implementation "com.google.android.gms:play-services-location:21.2.0"
        ii. implementation "com.squareup.retrofit2:retrofit:2.9.0"
        iii. implementation "com.squareup.retrofit2:converter-gson:2.9.0"
        iv. <string name="weather_api_key">your_string_api_key</string> // Get API Key link - https://home.openweathermap.org/api_keys

Screen Of this Application

![Nimbus Full Screenshot](https://github.com/swarup62/Nimbus/blob/dd9843d24780d53e743bc6fd01a62e669677725e/nimbus_full_screenshot.jpg?raw=true)
________________________________________

Project Structure 

1. Manifest File (AndroidManifest.xml)
•	Describes essential app information for the Android system.
•	Declares required permissions for location access and internet usage.

<uses-permission>: Declares the permissions the app requires, such as `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`, and `INTERNET`.
  <application>: Contains metadata about the application, including the icon, label, theme, and activities.
<activity>: Declares the activities that make up the app's user interface: `Splash`, `Home`, and `Aboutus`.
<intent-filter>: Specifies the entry point of the application (the `Splash` activity in this case).
<meta-data>: Includes metadata such as the Google Maps API key.

3. Layout Files
•	splash.xml (Splash Screen Layout): Displays the app logo and name.
•	home.xml (Home Screen Layout): 
          o	Displays weather data (temperature, location, date, time, conditions, etc.).
          o	Uses TextView, ImageView, CardView, and ConstraintLayout for UI structuring.
•	aboutus.xml (About Us Layout): Displays developer information and app details.

4. Java Source Files
•	Splash.java (Splash Activity) 
          o	Shows splash screen upon app launch.
          o	Uses a Handler to navigate to Home.java after a 2-second delay.
•	Home.java (Main Activity) 
          o	Manages UI elements and user interactions.
          o	Uses FusedLocationProviderClient to fetch the user's location.
          o	Converts coordinates into city names using Geocoder.
          o	Fetches weather data using Retrofit and updates UI.
          o	Displays forecast summaries and additional weather probabilities.
          o	Handles location permission requests.
•	Aboutus.java (About Us Activity) 
          o	Displays information about the app and developers.
5. Resources
•	strings.xml: Defines reusable strings such as app name, API keys, and UI labels. 
•	<string name="weather_api_key">your_string_api_key</string> // Get API Key link - https://home.openweathermap.org/api_keys 

6. Dependencies (Gradle Build Files)
Add the following dependencies in build.gradle:
        o implementation "com.google.android.gms:play-services-location:21.2.0"
        o implementation "com.squareup.retrofit2:retrofit:2.9.0"
        o implementation "com.squareup.retrofit2:converter-gson:2.9.0"
________________________________________
Installation & Setup
1.	Clone the repository: 
2.	git clone https://github.com/your-repo/nimbus-weather-app.git
3.	Open the project in Android Studio.
4.	Add your OpenWeatherMap API key in strings.xml.
5.	Build and run the application on an emulator or physical device.
________________________________________
License
This project is licensed under the MIT License.
________________________________________
Contributing
Feel free to contribute to this project by submitting issues or pull requests. Happy coding! 🚀

