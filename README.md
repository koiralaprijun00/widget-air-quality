# Nepal Weather Widget

A weather widget application for Nepal that shows current weather and air quality information.

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