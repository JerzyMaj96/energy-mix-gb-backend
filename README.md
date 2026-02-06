# Energy Mix Backend

The backend service for the Energy Mix project. It is a REST API built with **Java 21** and **Spring Boot**, acting as a logic and aggregation layer for the frontend dashboard.

The application consumes data from the public [Carbon Intensity UK API](https://carbonintensity.org.uk/), processes it (interval aggregation, averaging), and exposes it in a clean format for the frontend.

## Tech Stack

* **Language:** Java 21
* **Framework:** Spring Boot 3
* **Build Tool:** Maven
* **Containerization:** Docker (Multi-stage build)
* **External API:** Carbon Intensity API

## Key Features

1.  **Data Aggregation (3-Day Summary):**
    * Fetches raw data in 30-minute intervals.
    * Groups data by day and calculates the average percentage share of each fuel type (wind, solar, gas, nuclear, etc.).
    * Calculates the overall "Clean Energy" percentage for each day.

2.  **Optimal Charging Algorithm (Sliding Window):**
    * Analyzes the forecast for the next 48 hours.
    * Uses a sliding window algorithm to find a continuous block of time (e.g., 4 hours) with the highest average clean energy percentage.
    * Perfect for scheduling Electric Vehicle (EV) charging.

## Getting Started

### 1. Prerequisites
* Java 21 JDK
* Maven

### 2. Run Locally
```bash
mvn spring-boot:run
```
The server will start on port 8080 by default.

### 3. Run with Docker

The project includes a Dockerfile. You can build and run the image using:
docker build -t energy-mix-backend .
docker run -p 8080:8080 energy-mix-backend

## API Endpoints
Base URL: http://localhost:8080/energy-mix

GET	/three-days-summary -	Returns the energy mix summary for today and the next 2 days.

GET	/optimal-charging-window - Finds the best time window. Requires windowLength parameter (int), e.g., ?windowLength=4.

## Configuration
Key settings are located in src/main/resources/application.properties:

api.carbon-intensity.url - External API URL.

app.cors.allowed-origins - CORS configuration (defaults to allow http://localhost:5173 for Vite).

## Project Structure
```
src/main/java/com/jerzymaj/energymixgbbackend/
├── controller/    # EnergyMixController - API entry points
├── service/       # EnergyMixService - business logic and algorithms
├── DTOs/          # Data Transfer Objects (Records)
├── configuration/ # RestClient and CORS config
└── exceptions/    # Global Exception Handler
```
