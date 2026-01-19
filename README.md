# SmartScheduler ⚡
### Industrial Energy Optimization System

**SmartScheduler** is an Android application designed for industrial energy cost management. By integrating real-time market data with machine-specific parameters, it allows industrial facilities to shift high-power operations to hours with the lowest electricity prices, significantly reducing operational expenses.

---

## 🌟 Key Features

* **Real-time Energy Market Sync:** Automatically fetches RCE (Rynkowa Cena Energii) prices from the official **PSE** API via a dedicated testing middleware.
* **Interactive Data Visualization:** Dynamic bar charts representing daily price fluctuations with automated highlighting of cost extremes (cheapest/most expensive hours).
* **Machine Management (Local DB):** A robust system to store and manage machine profiles, including power consumption (kW) and required cycle duration (h), powered by **Room Database**.
* **Optimization Engine:** Implementation of an algorithm to calculate and find the most cost-effective continuous time slot for specific industrial processes.
* **Bidirectional Integration:** Verified capability to both fetch market data (GET) and push finalized production schedules (POST) to an external system for logging and execution.

---

## 🏗️ Architecture & Tech Stack

### Frontend (Android App)
* **Language:** Kotlin
* **UI Framework:** Jetpack Compose (Declarative UI)
* **Asynchronous Flow:** Coroutines & StateFlow for real-time reactive data streams.
* **Local Storage:** Room Persistence Library (SQLite).
* **Networking:** Retrofit 2 for API communication.

### Testing & Data Middleware (Python/FastAPI)
Located in the `/server` directory, this component serves as the testing infrastructure for bidirectional communication:
* **Dynamic Data Fetching:** The Python script automatically connects to the **PSE Portal**, fetches the latest energy price data, and parses it into a clean JSON format for the mobile app.
* **REST API Endpoints:** * `GET /prices`: Serves current PSE-sourced market data to the mobile client.
    * `POST /schedules`: Receives and logs JSON payloads containing calculated schedules sent from the smartphone to verify data integrity.

---

## ⚙️ How it Works

1.  **Synchronization:** The app connects to the Python middleware, which retrieves the latest 24-hour energy price list for the Polish market from PSE.
2.  **Optimization:** When a machine is selected, the **Sliding Window algorithm** scans the daily price array to find a continuous block of time that matches the machine's required work duration with the lowest total cost.
3.  **Bidirectional Feedback:** Once the operator confirms the plan, the app sends the schedule back to the server via a **POST request**, simulating a command to an automated factory control system.

---

## 📊 Data Source

This application relies on official market data provided by **PSE (Polskie Sieci Elektroenergetyczne)** via the Public Information Portal:
👉 [Official PSE Data Portal](https://api.raporty.pse.pl/app/home)

---
