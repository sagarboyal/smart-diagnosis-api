# smart-diagnosis-api

Smart Diagnosis API is a modular monolithic Spring Boot 3 service that accepts symptoms, generates possible conditions using Groq (llama3-8b-8192), and stores all diagnosis records in MongoDB.

## Stack
- Java 21
- Spring Boot 3
- MongoDB
- Groq API (OpenAI-compatible endpoint)
- Docker + Docker Compose

## API Endpoints
### `POST /diagnose`
Request body:
```json
{ "symptoms": "fever, cough, chest pain" }
```
Response (`201 Created`):
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

### `GET /history`
- Returns all records newest-first.
- Optional query parameter: `limit` (1 to 100).

Response (`200 OK`):
```json
{
  "total": 42,
  "records": []
}
```

## Architecture
```text
com.diagnosis
├── config
├── common
│   └── error
├── ai
│   ├── client
│   ├── config
│   ├── parser
│   └── service
├── diagnosis
│   ├── controller
│   ├── dto
│   ├── model
│   ├── repository
│   └── service
└── history
    ├── controller
    ├── dto
    └── service
```

- `ai` handles Groq prompt creation, HTTP calls, and response parsing.
- `diagnosis` handles POST flow + persistence.
- `history` returns past records.
- `common` includes global API error handling.

## Groq Integration
- Endpoint: `POST https://api.groq.com/openai/v1/chat/completions`
- Model: `llama3-8b-8192` (configurable)
- Auth: `Authorization: Bearer ${GROQ_API_KEY}`

The service enforces JSON-only output and includes a fallback condition list when Groq returns malformed output or the call fails, so `/diagnose` does not fail with a 500 due to AI formatting issues.

## Environment Variables
Use `.env` for local/docker execution.

Example `.env`:
```env
GROQ_API_KEY=your_groq_api_key_here
MONGO_URI=mongodb://mongodb:27017/diagnosisdb
SERVER_PORT=8080
```

## Quick Start
```bash
cp .env.example .env
# edit .env and set GROQ_API_KEY
docker compose up --build
```

## cURL Examples
```bash
curl -X POST http://localhost:8080/diagnose \
  -H "Content-Type: application/json" \
  -d '{"symptoms": "fever, cough, chest pain"}'
```

```bash
curl http://localhost:8080/history
curl "http://localhost:8080/history?limit=5"
```

## Error Format
All structured errors are returned as:
```json
{
  "timestamp": "2026-04-01T11:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "symptoms: symptoms is required",
  "path": "/diagnose"
}
```
