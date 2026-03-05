# Android AI Autopilot Backend

Production-ready backend for an Android AI Autopilot agent. This Express server uses the Google Gen AI SDK to communicate with Gemini Multimodal models for UI automation tasks. It is stateless but features optional Firestore session history memory, and it is fully deployable to Google Cloud Run.

## Architecture & Core Components
- **Express Server**: Receives POST `/agent/step` payloads containing a goal, a screenshot (Base64), and an accessibility UI tree (JSON).
- **Gemini Service**: Interacts with the Vertex AI via `@google/genai`. It enforces strict structured output rules using a system prompt.
- **Agent Service**: Orchestrates the communication between the controller, Firestore, and the Gemini model. Responsible for executing the safety layer heuristics.
- **Firestore Service**: Optional module to record session activities and goals tracking. Can be disabled via environment variables.

## Deployment to Google Cloud Run
This project includes a `Dockerfile` for easy deployment to Cloud Run.

1.  **Authenticate via Google Cloud CLI:**
    \`\`\`bash
    gcloud auth login
    gcloud config set project your-google-cloud-project-id
    \`\`\`

2.  **Deploy to Cloud Run directly from source:**
    \`\`\`bash
    gcloud run deploy autopilot-agent \\
      --source . \\
      --region us-central1 \\
      --allow-unauthenticated \\
      --set-env-vars="GOOGLE_CLOUD_PROJECT=your-google-cloud-project-id,GOOGLE_CLOUD_LOCATION=us-central1,FIRESTORE_ENABLED=true"
    \`\`\`

## Development
1. Install dependencies: \`npm install\`
2. Configure \`.env\`: Use \`.env.example\` as a template and provide your project ID.
3. Start locally in dev mode: \`npm run dev\`
