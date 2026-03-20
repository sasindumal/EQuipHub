# EQuipHub AI Integration & Next Steps Guide

This document outlines the architecture, setup requirements, and implementation strategies for adding AI capabilities to EQuipHub. 
We have already scaffolded the foundational Spring Boot Controller & Services (`AiController.java` & `AiIntegrationService.java`) to expose these endpoints to standard clients (Next.js & React Native).

---

## 🏗 System Architecture & Integrations

Adding AI without slowing down the existing Spring Boot + Postgres + Next.js stack requires external services for inference.

| AI Feature | Recommended Service | Backend Integration | Client Impact |
| --- | --- | --- | --- |
| **Smart Recommendations** | Simple Collaborative Filtering model in DB | Native Java + Postgres | React Native (Student Cart) |
| **Semantic Search** | OpenAI `text-embedding-3-small` / Gemini Embeddings | `pgvector` Extension in Postgres | Next.js & React Native Search Bars |
| **24/7 Virtual Assistant** | OpenAI `gpt-4o-mini` / Gemini Flash 1.5 | Spring AI (RAG Pipeline) | Next.js chat widget |
| **Computer Vision Damage** | AWS Rekognition / Google Cloud Vision | API Call from Spring Boot | React Native (Mobile Camera) |
| **Risk Scoring** | Machine Learning model (Python / Scikit) | Python microservice or Java rules engine | Lecturer dashboard (Web/App) |

---

## 💻 Scaffolded Implementation (What is Done)

We've laid the backend ground work by creating robust API structures inside your Spring Boot Backend:
1. `backend/api/src/main/java/com/equiphub/api/service/AiIntegrationService.java`: Business Logic for AI interactions with mock responses.
2. `backend/api/src/main/java/com/equiphub/api/controller/AiController.java`: A dedicated `/api/v1/ai/*` REST controller that handles requests from your frontends.

**Endpoints Added:**
* `GET /api/v1/ai/recommendations/{equipmentId}`
* `GET /api/v1/ai/search?query=...`
* `POST /api/v1/ai/chat`
* `POST /api/v1/ai/assess-damage`
* `POST /api/v1/ai/evaluate-risk`

---

## 🚀 Next Steps to Full Implementation

Because we cannot configure paid API keys autonomously, the following steps must be completed by a developer to fully operationalize the AI.

### Phase 1: Semantic Search & Chatbot via OpenAI / Spring AI
1. **Add Dependencies**: To the `backend/api/pom.xml`, add `spring-ai-openai-spring-boot-starter`.
2. **API Keys**: Add `spring.ai.openai.api-key=sk-...` to your `.env` or `application.yml`.
3. **Database Changes**: 
   * Enable the `vector` extension in your Neon Postgres Database using `CREATE EXTENSION vector;`.
   * Add a `embedding vector(1536)` column to your `Equipment` table.
4. **Implementation**: Edit `AiIntegrationService.java::semanticSearch` to query DB instances based on cosine similarity with an OpenAI-generated vector query.

### Phase 2: Computer Vision Damage Detection
1. **Set up AWS S3/Rekognition**: When the Technical Officer takes a photo of returned equipment via React Native Expo Camera, upload it to an S3 bucket or Google Cloud Storage.
2. **Comparison**: In `AiIntegrationService.java::assessDamage`, use the AWS SDK `CompareFaces` equivalent or Custom Labels Rekognition Model (if custom trained on camera/drone body damages) to compare the `baselineImageUrl` with `returnImageUrl`.
3. **Frontend**: The mobile app should notify the Technical Officer exactly where the discrepancy is using bounding boxes returned by AWS Rekognition.

### Phase 3: Demand Forecasting & Risk Scoring
1. **Metrics Export**: Export your `Request` and `Penalty` Postgres tables data to a CSV.
2. **Model Training (Optional)**: If you don't want to use standard heuristics (e.g. 3 late offences = 90% risk), you can create an external Python backend using Scikit-Learn that exposes a model which `evaluateRequestRisk()` can call.
3. **Rule-Based fallback**: Instead of ML, you can implement a standard weighted risk score inside Java using Penalty history.

### Phase 4: UI Development
1. **Next.js AI Chat Widget**: Build a floating chat icon component that maintains a state of `messages` holding `{ role: 'user' | 'assistant', content: string }`. Post to `/api/v1/ai/chat`.
2. **Expo React Native Component**: Ensure the Equipment Request screen hits `GET /api/v1/ai/recommendations/{equipmentId}` as you navigate to a specific item.

---

## Example RAG (Retrieval Augmented Generation) Workflow for Chatbot
If integrating Spring AI with your bot:
```java
// Inside AiIntegrationService.java

public String getChatbotResponse(String message, String userId) {
    // 1. Convert user's message to vector
    // 2. Query Postgres (pgvector) to find most relevant university equipment policies
    // 3. Form a strict Prompt System message: "You are the EQuipHub assistant. Answer only based on these policies: [Fetched Policy Text]..."
    // 4. Return the generation.
}
```

By completing the API connections outlined in Phase 1 & 2, you'll have an exceptionally modern Equipment Management System that predicts needs, automates heavy lifting for lecturers, and protects University assets with AI Vision.
