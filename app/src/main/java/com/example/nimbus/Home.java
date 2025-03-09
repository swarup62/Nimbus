package com.example.nimbus;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Query;
import com.example.nimbus.R;

public class Home extends AppCompatActivity {
    // Add these fields
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private FusedLocationProviderClient fusedLocationClient;
    private ImageView backButton, back, aboutUsButton, aboutus, sunMoonIcon, daysweathericon,
            previousweathericon, centerweathericon, tomorrowweathericon;
    private TextView daysReport, daysCurrentTime, daysCurrentDate, centerWeatherDay,
            previousWeatherDay, tomorrowWeatherDay, daysCurrentLocation, daysCurrentTemp,
            daysCurrentFeelsLike, daysweathertxt, centerweatherday, previousweatherday,
            tomorrowweatherday, dayscurrenttemp, dayscurrentfeelslike, dayscurrentloction,
            daysRainProb, daysWindProb, daysHumidityProb, daysPressureProb, daysUVProb, daysVisibilityProb,
            previousweathertemp, centerweathertemp, tomorrowweathertemp, sun_moon_position_1, 
            sun_moon_position_2, sun_moon_time_1, sun_moon_time_2;
    private CardView previousDayCard, tomorrowDayCard;
    private Calendar calendar;
    private SimpleDateFormat timeFormat, dateFormat;
    private String currentReport = "Today's Report";
    private SimpleDateFormat dayFormat;
    private LocationManager locationManager;
    private String city, state, country;
    private double currentTemp, feelsLikeTemp;
    private WeatherResponse weatherResponse;
    private View sunMoonPositionTrack;
    private ObjectAnimator iconAnimator;

    // Update this line
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/";

    // Move these interfaces and classes outside of fetchWeatherData method
    interface WeatherService {
        @GET("weather")
        Call<WeatherResponse> getCurrentWeather(
                @Query("lat") double lat,
                @Query("lon") double lon,
                @Query("appid") String apiKey,
                @Query("units") String units
        );
    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.home);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        initializeViews();
        setupDateTimeFormats();
        setupClickListeners();
        updateWeatherDays();
        updateCurrentDateTime();
        checkLocationPermission();
        updateLocation();

        // Start periodic updates of sun/moon position
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateSunMoonTimes();
                handler.postDelayed(this, 60000); // Update every minute
            }
        }, 0);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.home), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Home.this, "App Closing", Toast.LENGTH_SHORT).show();
                finish(); // Close the activity
            }
        });

        aboutus = findViewById(R.id.aboutus);
        aboutus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Home.this, Aboutus.class);
                startActivity(intent);
            }
        });

        daysReport = findViewById(R.id.daysreport);
        previousDayCard = findViewById(R.id.previousdaycard);
        tomorrowDayCard = findViewById(R.id.tomorrowcard);

        daysReport.setText(currentReport);

        previousDayCard.setOnClickListener(v -> updateUIForDay(-1));
        tomorrowDayCard.setOnClickListener(v -> updateUIForDay(1));
        
        CardView centerCard = findViewById(R.id.centercard);
        centerCard.setOnClickListener(v -> {
            currentReport = "Today's Report";
            daysReport.setText(currentReport);
            updateUI();
            updateWeatherProbabilities();
        });

        daysCurrentTime = findViewById(R.id.dayscurrenttime);
        calendar = Calendar.getInstance();
        timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

        updateTime();

        daysCurrentDate = findViewById(R.id.dayscurrentdate);
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        updateDate();

        centerweatherday = findViewById(R.id.centerweatherday);
        previousweatherday = findViewById(R.id.previousweatherday);
        tomorrowweatherday = findViewById(R.id.tomorrowweatherday);
        dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());

        updateDay();

        dayscurrentloction = findViewById(R.id.dayscurrentloction);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        updateLocation();

        dayscurrenttemp = findViewById(R.id.dayscurrenttemp);
        dayscurrentfeelslike = findViewById(R.id.days_current_feels_like);

        updateTemperature();
        updateFeelsLike();

        sun_moon_position_1 = findViewById(R.id.sun_moon_position_1);
        sun_moon_position_2 = findViewById(R.id.sun_moon_position_2);
        sun_moon_time_1 = findViewById(R.id.sun_moon_time_1);
        sun_moon_time_2 = findViewById(R.id.sun_moon_time_2);
    }

    private void initializeViews() {
        backButton = findViewById(R.id.back);
        aboutUsButton = findViewById(R.id.aboutus);
        sunMoonIcon = findViewById(R.id.sun_moon_icon);
        daysReport = findViewById(R.id.daysreport);
        daysCurrentTime = findViewById(R.id.dayscurrenttime);
        daysCurrentDate = findViewById(R.id.dayscurrentdate);
        centerWeatherDay = findViewById(R.id.centerweatherday);
        previousWeatherDay = findViewById(R.id.previousweatherday);
        tomorrowWeatherDay = findViewById(R.id.tomorrowweatherday);
        daysCurrentLocation = findViewById(R.id.dayscurrentloction);
        daysCurrentTemp = findViewById(R.id.dayscurrenttemp);
        daysCurrentFeelsLike = findViewById(R.id.days_current_feels_like);
        daysweathertxt = findViewById(R.id.days_weather_txt);
        daysRainProb = findViewById(R.id.days_rain_prob);
        daysWindProb = findViewById(R.id.days_wind_prob);
        daysHumidityProb = findViewById(R.id.days_humidity_prob);
        daysPressureProb = findViewById(R.id.days_pressure_prob);
        daysUVProb = findViewById(R.id.days_uv_prob);
        daysVisibilityProb = findViewById(R.id.days_visibility_prob);
        previousweathertemp = findViewById(R.id.previousweathertemp);
        centerweathertemp = findViewById(R.id.centerweathertemp);
        tomorrowweathertemp = findViewById(R.id.tomorrowweathertemp);
        sun_moon_position_1 = findViewById(R.id.sun_moon_position_1);
        sun_moon_position_2 = findViewById(R.id.sun_moon_position_2);
        sun_moon_time_1 = findViewById(R.id.sun_moon_time_1);
        sun_moon_time_2 = findViewById(R.id.sun_moon_time_2);
        sunMoonPositionTrack = findViewById(R.id.sun_moon_position_track);
        previousDayCard = findViewById(R.id.previousdaycard);
        tomorrowDayCard = findViewById(R.id.tomorrowcard);
        CardView centerCard = findViewById(R.id.centercard);
        daysweathericon = findViewById(R.id.daysweathericon);
        previousweathericon = findViewById(R.id.previousweathericon);
        centerweathericon = findViewById(R.id.centerweathericon);
        tomorrowweathericon = findViewById(R.id.tomorrowweathericon);
    }


    private void updateWeatherProbabilities() {
        // Different weather probabilities based on the current report
        double rainProb, windSpeed, humidityValue, pressureValue, uvValue, visibilityValue;
        String pressureTrend;
        
        if (currentReport.equals("Today's Report")) {
            // Use actual API data for today if available
            if (weatherResponse != null && weatherResponse.main != null) {
                // Handle rain probability - use actual value or minimum probability if available
                rainProb = (weatherResponse.main.rain_prob != null) ? weatherResponse.main.rain_prob : 0.1;
                // Ensure rain probability is shown as a percentage between 1-100%
                rainProb = Math.max(1.0, Math.min(100.0, rainProb * 100));
                
                // Handle wind speed - use actual value or minimum wind speed if available
                windSpeed = (weatherResponse.main.wind_speed != null) ? weatherResponse.main.wind_speed : 0.1;
                // Ensure wind speed is shown in km/h with a minimum of 0.1 km/h
                windSpeed = Math.max(0.1, windSpeed);
                
                // Handle humidity percentage
                humidityValue = Math.max(1.0, weatherResponse.main.humidity);
                
                // Handle pressure with up/down indicator
                pressureValue = (weatherResponse.main.pressure != null) ? weatherResponse.main.pressure : 1013.0; // Default standard pressure
                
                // Handle UV index with high/medium/low classification
                uvValue = (weatherResponse.main.uvi != null) ? weatherResponse.main.uvi : 0.0;
                
                // Handle visibility in kilometers
                visibilityValue = (weatherResponse.main.visibility != null) ? weatherResponse.main.visibility / 1000.0 : 10.0; // Convert meters to km, default 10km
            } else {
                // Default values when weather data is not available
                rainProb = 20.0;
                windSpeed = 5.0;
                humidityValue = 60.0;
                pressureValue = 1013.0;
                uvValue = 5.0;
                visibilityValue = 10.0;
            }
        } else if (currentReport.equals("Previous Day's Report")) {
            // Simulated data for previous day
            rainProb = 30.0; // 30% chance of rain
            windSpeed = 8.5; // 8.5 km/h
            humidityValue = 65.0; // 65%
            pressureValue = 1010.0; // 1010 mb (slightly below standard)
            uvValue = 4.0; // medium
            visibilityValue = 8.5; // 8.5 km
        } else if (currentReport.equals("Tomorrow's Report")) {
            // Simulated data for tomorrow
            rainProb = 10.0; // 10% chance of rain
            windSpeed = 6.2; // 6.2 km/h
            humidityValue = 55.0; // 55%
            pressureValue = 1015.0; // 1015 mb (slightly above standard)
            uvValue = 7.0; // high
            visibilityValue = 12.0; // 12 km
        } else {
            // Default values if something goes wrong
            rainProb = 20.0;
            windSpeed = 5.0;
            humidityValue = 60.0;
            pressureValue = 1013.0;
            uvValue = 5.0;
            visibilityValue = 10.0;
        }
        
        // Set pressure trend indicator
        pressureTrend = pressureValue > 1013.0 ? "↑" : "↓";
        
        // Format and update rain probability
        String rainProbText = String.format(Locale.getDefault(), "%.0f%%", rainProb);
        if (daysRainProb != null) daysRainProb.setText(rainProbText);
        
        // Format and update wind speed
        String windSpeedText = String.format(Locale.getDefault(), "%.1f km/h", windSpeed);
        if (daysWindProb != null) daysWindProb.setText(windSpeedText);
        
        // Format and update humidity
        String humidityText = String.format(Locale.getDefault(), "%.0f%%", humidityValue);
        if (daysHumidityProb != null) daysHumidityProb.setText(humidityText);
        
        // Format and update pressure with trend indicator
        String pressureText = String.format(Locale.getDefault(), "%.0f mb %s", pressureValue, pressureTrend);
        if (daysPressureProb != null) daysPressureProb.setText(pressureText);
        
        // Format and update UV index with level
        String uvLevel;
        if (uvValue >= 6.0) {
            uvLevel = "high";
        } else if (uvValue >= 3.0) {
            uvLevel = "medium";
        } else {
            uvLevel = "low";
        }
        String uvText = String.format(Locale.getDefault(), "%.0f %s", uvValue, uvLevel);
        if (daysUVProb != null) daysUVProb.setText(uvText);
        
        // Format and update visibility
        String visibilityText = String.format(Locale.getDefault(), "%.1f km", visibilityValue);
        if (daysVisibilityProb != null) daysVisibilityProb.setText(visibilityText);
    }

    private void setupDateTimeFormats() {
        calendar = Calendar.getInstance();
        timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    }

    private void setupClickListeners() {
        if (aboutUsButton != null) {
            aboutUsButton.setOnClickListener(v -> {
                startActivity(new Intent(Home.this, Aboutus.class));
            });
        }

        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                Toast.makeText(Home.this, "App Closing", Toast.LENGTH_SHORT).show();
                finish(); // Close the activity
            });
        }

        if (previousDayCard != null) {
            previousDayCard.setOnClickListener(v -> {
                // Update UI for previous day
                updateUIForDay(-1);
            });
        }
        
        if (tomorrowDayCard != null) {
            tomorrowDayCard.setOnClickListener(v -> {
                // Update UI for tomorrow
                updateUIForDay(1);
            });
        }
        
        CardView centerCard = findViewById(R.id.centercard);
        if (centerCard != null) {
            centerCard.setOnClickListener(v -> {
                // Update UI for today
                updateUIForDay(0);
            });
        }
    }

    private void showLocationDialog() {
        // Request location update instead of showing dialog
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            updateLocationUI(location);
                        }
                    });
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void updateWeatherDays() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());

        // Previous day
        cal.add(Calendar.DAY_OF_YEAR, -1);
        previousWeatherDay.setText(dayFormat.format(cal.getTime()));
        // For demo purposes using static value - replace with API call for historical data
        double previousDayTemp = 20.0; // Replace with actual API call
        previousweathertemp.setText(String.format(Locale.getDefault(), "%.0f° C", previousDayTemp));

        // Current day
        Calendar currentCal = Calendar.getInstance();
        centerWeatherDay.setText(dayFormat.format(currentCal.getTime()));
        centerweathertemp.setText(String.format(Locale.getDefault(), "%.0f° C", currentTemp));

        // Tomorrow
        Calendar tomorrowCal = Calendar.getInstance();
        tomorrowCal.add(Calendar.DAY_OF_YEAR, 1);
        tomorrowWeatherDay.setText(dayFormat.format(tomorrowCal.getTime()));
        // For demo purposes using static value - replace with API call for forecast
        double tomorrowTemp = 30.0; // Replace with actual API call
        tomorrowweathertemp.setText(String.format(Locale.getDefault(), "%.0f° C", tomorrowTemp));
    }

    private void updateCurrentDateTime() {
        daysCurrentTime.setText(timeFormat.format(calendar.getTime()));
        daysCurrentDate.setText(dateFormat.format(calendar.getTime()));
        daysReport.setText("Today's Report");
    }

    private void updateUIForDay(int dayOffset) {
        Calendar tempCal = (Calendar) calendar.clone();
        tempCal.add(Calendar.DAY_OF_YEAR, dayOffset);
        
        // Update the date display for the selected day
        daysCurrentDate.setText(dateFormat.format(tempCal.getTime()));
        
        // Simulated weather data for different days
        String weatherCondition;
        double rainProb, windSpeed, humidityValue, pressureValue, uvValue, visibilityValue;
        int weatherIconResource;
        boolean isNightTime = false;
        
        // Check if it's day or night for icon selection
        if (weatherResponse != null && weatherResponse.sys != null) {
            long currentTime = System.currentTimeMillis();
            long sunriseTimestamp = weatherResponse.sys.sunrise * 1000L;
            long sunsetTimestamp = weatherResponse.sys.sunset * 1000L;
            isNightTime = currentTime > sunsetTimestamp || currentTime < sunriseTimestamp;
        }
        
        if (dayOffset == -1) {
            // Set the current report to Previous Day
            currentReport = "Previous Day's Report";
            daysReport.setText(currentReport);
            
            // Update temperature and feels like for previous day
            currentTemp = 23.0;
            feelsLikeTemp = 24.0;
            
            // Weather condition for previous day
            weatherCondition = "Partly Cloudy";
            daysweathertxt.setText("It's " + weatherCondition);
            
            // Set simulated weather data for previous day
            rainProb = 30.0; // 30% chance of rain
            windSpeed = 8.5; // 8.5 km/h
            humidityValue = 65.0; // 65%
            pressureValue = 1010.0; // 1010 mb (slightly below standard)
            uvValue = 4.0; // medium
            visibilityValue = 8.5; // 8.5 km
            
            // Set weather icon for previous day
            weatherIconResource = isNightTime ? R.drawable.fewcloudsnight : R.drawable.fewcloudsday;
            daysweathericon.setImageResource(weatherIconResource);
            previousweathericon.setImageResource(weatherIconResource);
            centerweathericon.setImageResource(weatherIconResource);
            tomorrowweathericon.setImageResource(weatherIconResource);
            
        } else if (dayOffset == 1) {
            // Set the current report to Tomorrow
            currentReport = "Tomorrow's Report";
            daysReport.setText(currentReport);
            
            // Update temperature and feels like for next day
            currentTemp = 26.0;
            feelsLikeTemp = 28.0;
            
            // Weather condition for tomorrow
            weatherCondition = "Sunny";
            daysweathertxt.setText("It's " + weatherCondition);
            
            // Set simulated weather data for tomorrow
            rainProb = 10.0; // 10% chance of rain
            windSpeed = 6.2; // 6.2 km/h
            humidityValue = 55.0; // 55%
            pressureValue = 1015.0; // 1015 mb (slightly above standard)
            uvValue = 7.0; // high
            visibilityValue = 12.0; // 12 km
            
            // Set weather icon for tomorrow
            weatherIconResource = isNightTime ? R.drawable.clearskynight : R.drawable.clearskyday;
            daysweathericon.setImageResource(weatherIconResource);
            previousweathericon.setImageResource(weatherIconResource);
            centerweathericon.setImageResource(weatherIconResource);
            tomorrowweathericon.setImageResource(weatherIconResource);
            
        } else {
            // Set the current report to Today
            currentReport = "Today's Report";
            daysReport.setText(currentReport);
            
            // For today, we'll use the actual weather data from the API
            updateLocation(); // This will trigger a new weather update
            return; // Exit early as updateLocation will handle the UI update
        }
        
        // Update temperature displays
        dayscurrenttemp.setText(String.format("%.0f°", currentTemp));
        previousweathertemp.setText(String.format(Locale.getDefault(), "%.0f° C", currentTemp));
        centerweathertemp.setText(String.format(Locale.getDefault(), "%.0f° C", currentTemp));
        tomorrowweathertemp.setText(String.format(Locale.getDefault(), "%.0f° C", currentTemp));
        
        // Update feels like temperature
        daysCurrentFeelsLike.setText("Feels Like " + String.format("%.0f", feelsLikeTemp) + "° C");
        dayscurrentfeelslike.setText("Feels Like " + String.format("%.0f", feelsLikeTemp) + "° C");
        
        // Update all weather probabilities
        updateWeatherProbabilities();
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getCurrentLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE || requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
                    getCurrentLocation();
                } else {
                    updateLocation();
                }
            } else {
                String message = "Location permission denied";
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                dayscurrentloction.setText(message);
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        updateLocationUI(location);
                    }
                });
    }

    private void updateLocationUI(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    1
            );
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                String cityName = address.getLocality();
                String stateName = address.getAdminArea();
                String countryName = address.getCountryName();

                String fullLocation = String.format("%s, %s, %s",
                        cityName != null ? cityName : "",
                        stateName != null ? stateName : "",
                        countryName != null ? countryName : "");

                daysCurrentLocation.setText(fullLocation);

                // Update temperature and feels like temperature
                // For demo purposes using static values - replace with actual API data
                daysCurrentTemp.setText("25");
                daysCurrentFeelsLike.setText("Feels Like 27° C");

                // TODO: Implement weather API call to get real temperature data
                fetchWeatherData(location.getLatitude(), location.getLongitude());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class WeatherResponse {
        Main main;
        Weather[] weather;
        Sys sys;
        Weather[] previousWeather;
        Weather[] nextWeather;

        static class Sys {
            long sunrise;
            long sunset;
        }

        static class Main {
            double temp;
            double feels_like;
            double humidity;
            Double wind_speed = 0.0;
            Double rain_prob = 0.0;
            Double pressure = 1013.0;
            Double uvi = 0.0;
            Double visibility = 10000.0;
        }

        static class Weather {
            String main = "";
            String description = "";
        }
    }

    private void fetchWeatherData(double latitude, double longitude) {
        String apiKey = getString(R.string.weather_api_key);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WeatherService service = retrofit.create(WeatherService.class);
        Call<WeatherResponse> call = service.getCurrentWeather(latitude, longitude, apiKey, "metric");

        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    weatherResponse = response.body(); // Store the weather response
                    // Update temperature
                    currentTemp = weatherResponse.main.temp;
                    feelsLikeTemp = weatherResponse.main.feels_like;
                    updateTemperature();
                    updateFeelsLike();

                    // Update weather condition text
                    if (weatherResponse.weather != null && weatherResponse.weather.length > 0) {
                        String weatherDescription = weatherResponse.weather[0].description;
                        // Capitalize first letter and format the text
                        weatherDescription = weatherDescription.substring(0, 1).toUpperCase() + weatherDescription.substring(1);
                        daysweathertxt.setText("It's " + weatherDescription);
                    }
                    // Call updateWeatherUI to update all weather icons
                    updateWeatherUI(weatherResponse);
                    updateWeatherProbabilities();
                    updateSunMoonTimes();
                } else {
                    Toast.makeText(Home.this, "Error fetching weather data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                Toast.makeText(Home.this, "Failed to fetch weather data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateWeatherUI(WeatherResponse weather) {
        if (weather == null || weather.main == null) {
            // Handle null weather data
            return;
        }
        
        daysCurrentTemp.setText(String.valueOf(Math.round(weather.main.temp)));
        daysCurrentFeelsLike.setText("Feels Like " + Math.round(weather.main.feels_like) + "° C");

        if (weather.weather != null && weather.weather.length > 0) {
            String condition = weather.weather[0].description.toLowerCase();
            String capitalizedCondition = condition.substring(0, 1).toUpperCase() + condition.substring(1);
            daysweathertxt.setText("It's " + capitalizedCondition);

            // Update the current temperature and feels like temperature
            currentTemp = weather.main.temp;
            feelsLikeTemp = weather.main.feels_like;

            // Check if it's day or night
            boolean isNightTime = false;
            if (weatherResponse != null && weatherResponse.sys != null) {
                long currentTime = System.currentTimeMillis();
                long sunriseTimestamp = weatherResponse.sys.sunrise * 1000L;
                long sunsetTimestamp = weatherResponse.sys.sunset * 1000L;
                isNightTime = currentTime > sunsetTimestamp || currentTime < sunriseTimestamp;
            }

            // Use the class variables for weather icons instead of finding them each time
            TextView excessiveHeatTextView = findViewById(R.id.excessive_heat);

            // Simulate weather conditions for previous and next days
            String previousCondition = "few clouds";
            String nextCondition = "clear sky";

            // Get the current condition
             if (weather.weather != null && weather.weather.length > 0) {
                condition = weather.weather[0].description.toLowerCase();
             }

            // Update weather icon for the current day
            int weatherIconResource = R.drawable.sun;
            if (condition.contains("clear")) {
                weatherIconResource = isNightTime ? R.drawable.clearskynight : R.drawable.clearskyday;
            } else if (condition.contains("few clouds")) {
                weatherIconResource = isNightTime ? R.drawable.fewcloudsnight : R.drawable.fewcloudsday;
            } else if (condition.contains("scattered clouds") || condition.contains("broken clouds") || condition.contains("overcast clouds")) {
                weatherIconResource = R.drawable.scattered_broken_overcast_clouds_day_night;
            } else if (condition.contains("light rain")) {
                weatherIconResource = R.drawable.light_rain_day_night;
            } else if (condition.contains("moderate rain") || condition.contains("heavy rain")) {
                weatherIconResource = R.drawable.heavy_moderate_rain_day_night;
            } else if (condition.contains("thunderstorm")) {
                weatherIconResource = R.drawable.thunderstorm_day_night;
            } else if (condition.contains("snow")) {
                weatherIconResource = R.drawable.snow_day_night;
            } else if (condition.contains("mist") || condition.contains("fog")) {
                weatherIconResource = R.drawable.mist_fog_day_night;
            } else if (condition.contains("drizzle")) {
                weatherIconResource = R.drawable.drizzle_day_night;
            } else if (condition.contains("shower rain") || (condition.contains("rain") && condition.contains("snow"))) {
                weatherIconResource = R.drawable.shower_rain_day_night;
            } else if (condition.contains("haze")) {
                weatherIconResource = R.drawable.haze_day_night;
            } else {
                weatherIconResource = isNightTime ? R.drawable.sun : R.drawable.moon;
            }
            daysweathericon.setImageResource(weatherIconResource);

            // Update previous day weather icon
            int previousWeatherIconResource = R.drawable.sun;
            if (previousCondition.contains("clear")) {
                previousWeatherIconResource = isNightTime ? R.drawable.clearskynight : R.drawable.clearskyday;
            } else if (previousCondition.contains("few clouds")) {
                previousWeatherIconResource = isNightTime ? R.drawable.fewcloudsnight : R.drawable.fewcloudsday;
            } else if (previousCondition.contains("scattered clouds") || previousCondition.contains("broken clouds") || previousCondition.contains("overcast clouds")) {
                previousWeatherIconResource = R.drawable.scattered_broken_overcast_clouds_day_night;
            } else if (previousCondition.contains("light rain")) {
                previousWeatherIconResource = R.drawable.light_rain_day_night;
            } else if (previousCondition.contains("moderate rain") || previousCondition.contains("heavy rain")) {
                previousWeatherIconResource = R.drawable.heavy_moderate_rain_day_night;
            } else if (previousCondition.contains("thunderstorm")) {
                previousWeatherIconResource = R.drawable.thunderstorm_day_night;
            } else if (previousCondition.contains("snow")) {
                previousWeatherIconResource = R.drawable.snow_day_night;
            } else if (previousCondition.contains("mist") || previousCondition.contains("fog")) {
                previousWeatherIconResource = R.drawable.mist_fog_day_night;
            } else if (previousCondition.contains("drizzle")) {
                previousWeatherIconResource = R.drawable.drizzle_day_night;
            } else if (previousCondition.contains("shower rain") || (previousCondition.contains("rain") && previousCondition.contains("snow"))) {
                previousWeatherIconResource = R.drawable.shower_rain_day_night;
            } else if (previousCondition.contains("haze")) {
                previousWeatherIconResource = R.drawable.haze_day_night;
            } else {
                previousWeatherIconResource = isNightTime ? R.drawable.sun : R.drawable.moon;
            }
            previousweathericon.setImageResource(previousWeatherIconResource);

            // Update next day weather icon
            int tomorrowWeatherIconResource = R.drawable.sun;
            if (nextCondition.contains("clear")) {
                tomorrowWeatherIconResource = isNightTime ? R.drawable.clearskynight : R.drawable.clearskyday;
            } else if (nextCondition.contains("few clouds")) {
                tomorrowWeatherIconResource = isNightTime ? R.drawable.fewcloudsnight : R.drawable.fewcloudsday;
            } else if (nextCondition.contains("scattered clouds") || nextCondition.contains("broken clouds") || nextCondition.contains("overcast clouds")) {
                tomorrowWeatherIconResource = R.drawable.scattered_broken_overcast_clouds_day_night;
            } else if (nextCondition.contains("light rain")) {
                tomorrowWeatherIconResource = isNightTime ? R.drawable.light_rain_day_night : R.drawable.light_rain_day_night;
            } else if (nextCondition.contains("moderate rain") || nextCondition.contains("heavy rain")) {
                tomorrowWeatherIconResource = R.drawable.heavy_moderate_rain_day_night;
            } else if (nextCondition.contains("thunderstorm")) {
                tomorrowWeatherIconResource = R.drawable.thunderstorm_day_night;
            } else if (nextCondition.contains("snow")) {
                tomorrowWeatherIconResource = R.drawable.snow_day_night;
            } else if (nextCondition.contains("mist") || nextCondition.contains("fog")) {
                tomorrowWeatherIconResource = R.drawable.mist_fog_day_night;
            } else if (nextCondition.contains("drizzle")) {
                tomorrowWeatherIconResource = R.drawable.drizzle_day_night;
            } else if (nextCondition.contains("shower rain") || (nextCondition.contains("rain") && nextCondition.contains("snow"))) {
                tomorrowWeatherIconResource = R.drawable.shower_rain_day_night;
            } else if (nextCondition.contains("haze")) {
                tomorrowWeatherIconResource = R.drawable.haze_day_night;
            } else {
                tomorrowWeatherIconResource = isNightTime ? R.drawable.sun : R.drawable.moon;
            }
            tomorrowweathericon.setImageResource(tomorrowWeatherIconResource);

            // Update center day weather icon
            int centerWeatherIconResource = R.drawable.sun;
            if (condition.contains("clear")) {
                centerWeatherIconResource = isNightTime ? R.drawable.clearskynight : R.drawable.clearskyday;
            } else if (condition.contains("few clouds")) {
                centerWeatherIconResource = isNightTime ? R.drawable.fewcloudsnight : R.drawable.fewcloudsday;
            } else if (condition.contains("scattered clouds") || condition.contains("broken clouds") || condition.contains("overcast clouds")) {
                centerWeatherIconResource = R.drawable.scattered_broken_overcast_clouds_day_night;
            } else if (condition.contains("light rain")) {
                centerWeatherIconResource = isNightTime ? R.drawable.light_rain_day_night : R.drawable.light_rain_day_night;
            } else if (condition.contains("moderate rain") || condition.contains("heavy rain")) {
                centerWeatherIconResource = R.drawable.heavy_moderate_rain_day_night;
            } else if (condition.contains("thunderstorm")) {
                centerWeatherIconResource = R.drawable.thunderstorm_day_night;
            } else if (condition.contains("snow")) {
                centerWeatherIconResource = R.drawable.snow_day_night;
            } else if (condition.contains("mist") || condition.contains("fog")) {
                centerWeatherIconResource = R.drawable.mist_fog_day_night;
            } else if (condition.contains("drizzle")) {
                centerWeatherIconResource = R.drawable.drizzle_day_night;
            } else if (condition.contains("shower rain") || (condition.contains("rain") && condition.contains("snow"))) {
                centerWeatherIconResource = R.drawable.shower_rain_day_night;
            } else if (condition.contains("haze")) {
                centerWeatherIconResource = R.drawable.haze_day_night;
            } else {
                centerWeatherIconResource = isNightTime ? R.drawable.sun : R.drawable.moon;
            }
            centerweathericon.setImageResource(centerWeatherIconResource);

            // Check for excessive heat condition
            if (condition.contains("hot") || currentTemp > 35) {
                excessiveHeatTextView.setVisibility(View.VISIBLE);
            } else {
                excessiveHeatTextView.setVisibility(View.GONE);
            }
        }
    }

    private void updateUI() {
        updateTime();
        updateDate();
        updateDay();
        
        // Only update location, temperature and feels like for Today's Report
        if (currentReport.equals("Today's Report")) {
            updateLocation();
            updateTemperature();
            updateFeelsLike();
            
            // If we're viewing today's report, use the API data
            if (weatherResponse != null) {
                updateWeatherUI(weatherResponse);
            }
        }
        // For previous or next day, we don't update these values as they're set in updateUIForDay
    }

    private void updateTime() {
        if (currentReport.equals("Today's Report")) {
            daysCurrentTime.setText(timeFormat.format(calendar.getTime()));
        } else if (currentReport.equals("Previous Day's Report")) {
            // Calculate previous day's time (example: same time as today)
            Calendar prevDay = Calendar.getInstance();
            prevDay.add(Calendar.DAY_OF_MONTH, -1);
            daysCurrentTime.setText(timeFormat.format(prevDay.getTime()));
        } else if (currentReport.equals("Tomorrow's Report")) {
            // Calculate tomorrow's time (example: same time as today)
            Calendar nextDay = Calendar.getInstance();
            nextDay.add(Calendar.DAY_OF_MONTH, 1);
            daysCurrentTime.setText(timeFormat.format(nextDay.getTime()));
        }
    }

    private void updateDate() {
        if (currentReport.equals("Today's Report")) {
            daysCurrentDate.setText(dateFormat.format(calendar.getTime()));
        } else if (currentReport.equals("Previous Day's Report")) {
            Calendar prevDay = Calendar.getInstance();
            prevDay.add(Calendar.DAY_OF_MONTH, -1);
            daysCurrentDate.setText(dateFormat.format(prevDay.getTime()));
        } else if (currentReport.equals("Tomorrow's Report")) {
            Calendar nextDay = Calendar.getInstance();
            nextDay.add(Calendar.DAY_OF_MONTH, 1);
            daysCurrentDate.setText(dateFormat.format(nextDay.getTime()));
        }
    }

    private void updateDay() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());

        // Previous day
        cal.add(Calendar.DAY_OF_YEAR, -1);
        previousweatherday.setText(dayFormat.format(cal.getTime()));

        // Reset to current day
        cal.add(Calendar.DAY_OF_YEAR, 1);
        centerweatherday.setText(dayFormat.format(cal.getTime()));

        // Tomorrow
        cal.add(Calendar.DAY_OF_YEAR, 1);
        tomorrowweatherday.setText(dayFormat.format(cal.getTime()));
    }

    private void updateLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    100);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        updateLocationUI(location);
                    }
                });
    }

    private void updateTemperature() {
        if (currentReport.equals("Today's Report")) {
            // Use the temperature from the weather API response
            dayscurrenttemp.setText(String.format("%.0f", currentTemp));
            centerweathertemp.setText(String.format(Locale.getDefault(), "%.0f° C", currentTemp));
        } else if (currentReport.equals("Previous Day's Report")) {
            // For demo purposes using static value - replace with API call for historical data
            currentTemp = 23.0;
            dayscurrenttemp.setText(String.format("%.0f", currentTemp));
            centerweathertemp.setText(String.format(Locale.getDefault(), "%.0f° C", currentTemp));
        } else if (currentReport.equals("Tomorrow's Report")) {
            // For demo purposes using static value - replace with API call for forecast
            currentTemp = 26.0;
            dayscurrenttemp.setText(String.format("%.0f", currentTemp));
            centerweathertemp.setText(String.format(Locale.getDefault(), "%.0f° C", currentTemp));
        }
    }

    private void updateFeelsLike() {
        if (currentReport.equals("Today's Report")) {
            // Use the feels like temperature from the weather API response
            dayscurrentfeelslike.setText("Feels Like " + String.format("%.0f", feelsLikeTemp) + "° C");
            daysCurrentFeelsLike.setText("Feels Like " + String.format("%.0f", feelsLikeTemp) + "° C");
        } else if (currentReport.equals("Previous Day's Report")) {
            // For demo purposes using static value - replace with API call for historical data
            feelsLikeTemp = 24.0;
            dayscurrentfeelslike.setText("Feels Like " + String.format("%.0f", feelsLikeTemp) + "° C");
            daysCurrentFeelsLike.setText("Feels Like " + String.format("%.0f", feelsLikeTemp) + "° C");
        } else if (currentReport.equals("Tomorrow's Report")) {
            // For demo purposes using static value - replace with API call for forecast
            feelsLikeTemp = 28.0;
            dayscurrentfeelslike.setText("Feels Like " + String.format("%.0f", feelsLikeTemp) + "° C");
            daysCurrentFeelsLike.setText("Feels Like " + String.format("%.0f", feelsLikeTemp) + "° C");
        }
    }


    private void updateSunMoonTimes() {
        if (weatherResponse != null && weatherResponse.sys != null) {
            long currentTime = System.currentTimeMillis();
            long sunriseTimestamp = weatherResponse.sys.sunrise * 1000L;
            long sunsetTimestamp = weatherResponse.sys.sunset * 1000L;

            // Update icon based on time
            boolean isNightTime = currentTime > sunsetTimestamp || currentTime < sunriseTimestamp;
            sunMoonIcon.setImageResource(isNightTime ? R.drawable.moon : R.drawable.sun);
            sunMoonIcon.setVisibility(View.VISIBLE);

            // Calculate animation progress
            float progress;
            if (isNightTime) {
                // Night time: animate between moonrise and moonset
                progress = (float) (currentTime - sunsetTimestamp) / (sunsetTimestamp - sunriseTimestamp);

                // Set default positions for moon when timing doesn't match
                if (progress < 0) {
                    progress = 0; // Start position for moon (sunset)
                } else if (progress > 1) {
                    progress = 1; // End position for moon (sunset)
                }
            } else {
                // Day time: animate between sunrise and sunset
                progress = (float) (currentTime - sunriseTimestamp) / (sunsetTimestamp - sunriseTimestamp);

                // Set default positions for sun when timing doesn't match
                if (progress < 0) {
                    progress = 0; // Start position for sun (sunrise)
                } else if (progress > 1) {
                    progress = 1; // End position for sun (sunset)
                }
            }

            // Animate icon position
            if (iconAnimator != null) {
                iconAnimator.cancel();
            }

            float trackWidth = sunMoonPositionTrack.getWidth() - sunMoonIcon.getWidth();
            float targetX = trackWidth * progress;

            iconAnimator = ObjectAnimator.ofFloat(sunMoonIcon, "translationX", targetX);
            iconAnimator.setDuration(500); // Half second animation
            iconAnimator.start();

            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            timeFormat.setTimeZone(TimeZone.getDefault());
    
            // Format all times
            String sunriseTime = timeFormat.format(new Date(sunriseTimestamp));
            String sunsetTime = timeFormat.format(new Date(sunsetTimestamp));
    
            // Determine which to show based on current time
            boolean showingSunrise = Math.abs(currentTime - sunriseTimestamp) < Math.abs(currentTime - sunsetTimestamp);
            boolean showingSunset = Math.abs(currentTime - sunsetTimestamp) < Math.abs(currentTime - sunriseTimestamp);
    
            // Update the TextViews
            if (showingSunrise) {
                sun_moon_position_1.setText("Sunrise");
                sun_moon_time_1.setText(sunriseTime);
            } else {
                sun_moon_position_1.setText("Sunset");
                sun_moon_time_1.setText(sunsetTime);
            }
    
            if (showingSunset) {
                sun_moon_position_2.setText("Sunset");
                sun_moon_time_2.setText(sunsetTime);
            } else {
                sun_moon_position_2.setText("Sunrise");
                sun_moon_time_2.setText(sunriseTime);
            }
        } else {
            // Handle the case where sunrise/sunset data is not available
            sunMoonIcon.setVisibility(View.VISIBLE);
            sunMoonIcon.setTranslationX(0); // Default to start position
            sunMoonIcon.setImageResource(R.drawable.sun); // Default to sun
            
            sun_moon_position_1.setText("N/A");
            sun_moon_time_1.setText("N/A");
            sun_moon_position_2.setText("N/A");
            sun_moon_time_2.setText("N/A");
        }
    }
}