# Operon

Operon is an experimental AI Agent Copilot that acts as a generalized assistant for your Android device. Driven by Google's Gemini 2.5 models (Pro and Flash), Operon utilizes the Android Accessibility Service and MediaProjection APIs to "see" your screen and execute actions (clicking, typing, scrolling, navigating) on your behalf to achieve complex goals across any app.

The project is split into two primary components:
1. **Operon-Backend**: A robust TypeScript/Express server that securely interfaces with Google Cloud Vertex AI to perform visual and structural reasoning on UI trees.
2. **Operon-Mobile**: The native Android application (built with Jetpack Compose) that captures screen states, requests instructions from the backend, and executes them autonomously.

---

## 🏗️ Architecture Overview

The system operates in a closed continuous loop:
1. **State Capture**: `Operon-Mobile` takes a real-time screenshot (via `MediaProjection`) and a structural UI layout tree (via `AccessibilityService`).
2. **Analysis**: The payload is sent to `Operon-Backend`.
3. **Reasoning**: The backend constructs a prompt including the user's goal, the UI JSON tree, and the base64 screenshot. Gemini evaluates the state and outputs a strict JSON instruction.
4. **Execution**: The Android app parses the JSON action (e.g., `click` at bounds `X,Y`, `input_text` "hello", `home`, `scroll`) and executes it using the Accessibility API.
5. **Approval**: For highly sensitive actions (e.g., "submit", "send message", "delete"), the agent pauses and spawns a Global Overlay over your current app to request manual approval before proceeding.

---

## 🚀 Getting Started

### Prerequisites

- **Node.js** (v18 or higher)
- **Android Studio** (Koala or newer recommended)
- **Google Cloud Platform (GCP) Account** with Vertex AI API enabled.
- A physical Android device (Android 14 / API 34+ recommended) for testing, as Emulators often struggle with precise MediaProjection and Accessibility Services.

---

### 1. Setting up the Backend (`Operon-Backend`)

The backend is responsible for all LLM interactions and requires Google Cloud authentication.

1. **Navigate to the backend directory:**
   ```bash
   cd Operon-Backend
   ```

2. **Install dependencies:**
   ```bash
   npm install
   ```

3. **Configure Environment Variables:**
   Copy the example environment file:
   ```bash
   cp .env.example .env
   ```
   Open `.env` and fill in your Google Cloud details:
   ```env
   PORT=3000
   GOOGLE_CLOUD_PROJECT="your-gcp-project-id"
   GOOGLE_CLOUD_LOCATION="us-central1"
   # Determine which Gemini model to hit. Set to 'DEV' for Flash (faster/cheaper) or 'LIVE' for Pro (smarter).
   APP_ENV="DEV"
   ```

4. **Authenticate with Google Cloud:**
   You must have the `gcloud` CLI installed and authenticated with Application Default Credentials (ADC) so the Node.js SDK can access Vertex AI.
   ```bash
   gcloud auth application-default login
   ```
   *(Ensure the account you log in with has the `Vertex AI User` role in your GCP project).*

5. **Run the server:**
   ```bash
   # For development (auto-restarts on changes)
   npm run dev

   # For production build
   npm run build
   npm start
   ```
   The server will start on `http://localhost:3000`.

---

### 2. Setting up the Mobile App (`Operon-Mobile`)

The mobile client intercepts screen data and executes native accessibility commands.

1. **Open the Project:**
   Open Android Studio and select the `Operon/Operon-Mobile` folder as your project. Allow Gradle to sync.

2. **Configure Local Network Access:**
   Because your Android device needs to talk to your local backend, you need to point the app to your computer's local IP address.
   - Find your computer's local IPv4 address (e.g., `192.168.1.50`).
   - Open `Operon-Mobile/app/src/main/java/com/mardillu/operon/api/NetworkModule.kt`.
   - Update the `BASE_URL` to point to your computer:
     ```kotlin
     private const val BASE_URL = "http://192.168.1.50:3000/"
     ```

3. **Build and Install:**
   - Connect your physical Android device.
   - Click the green **Run** button in Android Studio (or run `./gradlew assembleDebug`).

4. **Onboarding & Permissions:**
   Upon first launch, Operon will ask for three distinct, deeply sensitive system permissions:
   - **Accessibility Service**: Allows the app to read UI elements and perform clicks/swipes. You must manually enable "Operon" in your device's Accessibility Settings.
   - **Screen Recording (MediaProjection)**: Allows the app to see your screen visually.
   - **Draw Over Other Apps (SYSTEM_ALERT_WINDOW)**: Allows the app to pause operations and show you an approval dialog globally, no matter what app you are currently in.

---

## ⚙️ Execution Modes & Safeguards

You can configure how much autonomy you want to give the Operon agent via the Settings gear icon on the Home screen:

- **Strict Approval (Always Ask)**: The agent pauses and asks for permission before executing *every single action*. Best for debugging.
- **Recommended (Ask for Risky Actions)**: The agent operates autonomously for basic navigation, reading, and swiping. It will only pause and spawn a Global Overlay dialog if it attempts a destructive/committal action (e.g., clicking "Send", "Submit", "Delete", or typing sensitive data).
- **Full Autonomy (Never Ask)**: The agent acts completely on its own without interrupting you. *Use with extreme caution.*

---

## 🛠️ Tech Stack & Dependencies

**Backend:**
- TypeScript
- Express.js
- `@google/genai` (Official Google Vertex AI SDK)
- `zod` (Input Validation)

**Mobile:**
- Kotlin
- Jetpack Compose (Modern native UI)
- Coroutines & Flow (Asynchronous state management)
- Retrofit & OkHttp (Networking)
- Jetpack DataStore (Preferences persistence)
- Android API: `AccessibilityService`, `MediaProjection`, `WindowManager`.
