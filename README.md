# 🩺 Smart Diagnosis API

A modular monolithic **Spring Boot 3** service that accepts patient symptoms, generates possible medical conditions using **Groq (LLaMA 3)**, and stores all diagnosis records in **MongoDB**.

---

## 🚀 Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 21 |
| Framework | Spring Boot 3 |
| Database | MongoDB |
| AI Provider | Groq API — `llama-3.3-70b-versatile` |
| Containerization | Docker + Docker Compose |

---

## 📡 API Endpoints

### `POST /diagnose`
Accepts a list of symptoms and returns AI-generated possible conditions.

**Request Body:**
```json
{ "symptoms": "fever, cough, chest pain" }
```

**Response `201 Created`:**
```json
{
  "id": "64abc123...",
  "symptoms": "fever, cough, chest pain",
  "timestamp": "2026-04-01T10:30:00",
  "conditions": [
    {
      "name": "Pneumonia",
      "probabilityPercent": 65,
      "doctorType": "Pulmonologist",
      "nextSteps": ["Chest X-ray", "Blood culture", "CBC test"]
    }
  ]
}
```

---

### `GET /history`
Returns all past diagnosis records, newest first.

**Optional Query Parameter:** `limit` (1–100)

**Response `200 OK`:**
```json
{
  "total": 42,
  "records": ["..."]
}
```

**Examples:**
```bash
curl http://localhost:8080/history
curl "http://localhost:8080/history?limit=5"
```

---

## ⚠️ Error Format

All structured errors follow this format:

```json
{
  "timestamp": "2026-04-01T11:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "symptoms: symptoms is required",
  "path": "/diagnose"
}
```

---

## 🏗️ Project Architecture

```
com.diagnosis
├── config                  # App-level configuration
├── common
│   └── error               # Global exception handling
├── ai
│   ├── client              # Groq HTTP client
│   ├── config              # AI model settings
│   ├── parser              # Response parsing + fallback
│   └── service             # Prompt building + orchestration
├── diagnosis
│   ├── controller          # POST /diagnose
│   ├── dto                 # Request/Response DTOs
│   ├── model               # MongoDB document model
│   ├── repository          # MongoDB repository
│   └── service             # Core diagnosis logic
└── history
    ├── controller          # GET /history
    ├── dto                 # History response DTOs
    └── service             # Record retrieval logic
```

The service is structured as a **modular monolith** — the diagnosis flow, history flow, AI client logic, and error handling are all isolated without adding distributed-system complexity.

---

## 🤖 AI Integration

- **Provider:** Groq (OpenAI-compatible endpoint)
- **Endpoint:** `POST https://api.groq.com/openai/v1/chat/completions`
- **Model:** `llama-3.3-70b-versatile` (configurable via `GROQ_MODEL`)
- **Auth:** `Authorization: Bearer ${GROQ_API_KEY}`

The `ai` package handles:
1. Building a structured medical prompt from symptoms
2. Calling Groq's chat completions API
3. Parsing the JSON response into condition DTOs
4. **Fallback handling** — if Groq returns malformed output or the call fails, a fallback condition list is returned so `/diagnose` never crashes with a 500

---

## ⚙️ Environment Variables

Create a `.env` file in the project root:

```env
GROQ_API_KEY=your_groq_api_key_here
MONGO_URI=mongodb://localhost:27017/diagnosisdb
SERVER_PORT=8080
```

> When using Docker Compose with the bundled MongoDB container, `MONGO_URI` is automatically overridden to `mongodb://mongodb:27017/diagnosisdb` inside the Compose network.
> If you want to use MongoDB Atlas instead, set `MONGO_URI` to your Atlas connection string and remove the `MONGO_URI` override from `docker-compose.yml`.

---

## 🐳 Quick Start (Docker)

```bash
# 1. Copy env file and add your Groq API key
cp .env.example .env

# 2. Start all services
docker compose up --build
```

**Services after startup:**

| Service | URL |
|---------|-----|
| API | http://localhost:8080 |
| MongoDB | mongodb://localhost:27017 |
| Mongo Express (UI) | http://localhost:8081 |

---

## 🧪 cURL Examples

**Diagnose:**
```bash
curl -X POST http://localhost:8080/diagnose \
  -H "Content-Type: application/json" \
  -d '{"symptoms": "fever, cough, chest pain"}'
```

**History:**
```bash
curl http://localhost:8080/history
curl "http://localhost:8080/history?limit=5"
```

---

## 📁 Repository Structure

```
smart-diagnosis-api/
├── src/
│   └── main/java/com/diagnosis/
├── docker-compose.yml
├── Dockerfile
├── .env.example
└── README.md
```

---

## 🧠 How AI Integration Works

When someone sends their symptoms to `/diagnose`, here is what happens step by step:

1. **You send symptoms** — something like `"fever, cough, chest pain"` hits the API
2. **The app builds a prompt** — it wraps those symptoms into a structured medical question, telling the AI: *"here are the symptoms, reply with possible conditions in JSON format only"*
3. **It calls Groq** — the app sends the prompt to Groq's OpenAI-compatible API, which runs the configured LLaMA model and returns a completion
4. **The response gets parsed** — the AI replies with something like *"could be Pneumonia, 65% chance, see a Pulmonologist"* and the app converts that into a clean structured format
5. **Fallback safety net** — sometimes AI returns messy or broken output. Instead of crashing, the app has a backup plan — it returns a default safe response so the API never breaks with a 500 error
6. **Saved to MongoDB** — once the diagnosis is ready, the full record (symptoms + conditions + timestamp) is saved to the database
7. **You get the response** — the clean JSON is returned to whoever called the API
8. **History anytime** — anyone can call `/history` later to see all past diagnoses, newest first

> Think of it like this: **the app is an assistant layer around the model** — it formats the request, validates the AI output, stores the result, and exposes it through a stable API.

---

## 👤 Author

**Sagar Boyal**  
[GitHub](https://github.com/sagarboyal) · [LinkedIn](https://linkedin.com/in/sagarboyal)
