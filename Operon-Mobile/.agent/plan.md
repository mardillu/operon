# Project Plan

Create a modern Android app called “Autopilot”, an AI system-level assistant that can operate the phone on behalf of the user (click, scroll, navigate apps) using Accessibility permissions.

Main Experience

Home Screen
- Dark mode default
- Large centered mic button (primary action)
- Subtle animated pulse when listening
- Text below mic: “What should I do?”
- Small “Recent Commands” section
- Status indicator at top: “Autopilot Ready” (green dot)

Live Execution Screen (Core Experience)
- Semi-transparent overlay panel (bottom sheet style)
- Shows:
    - Current user instruction
    - Step-by-step execution log
    - “Thinking…” animation
    - Next predicted action
- Small floating stop button (red)
- Minimal distraction
- Feels like the AI is calmly narrating actions

Floating HUD Mode
- Small draggable circular bubble
- Expands into mini control panel
- Shows:
    - Listening state
    - Pause
    - Stop
    - View reasoning

Permission Setup Flow
- Clean onboarding
- Explain:
    - Accessibility permission
    - Screen capture permission
- Use clear visuals, not heavy text
- Show diagrams of how it works

## Project Brief

# Project Brief: Autopilot

Autopilot is a cutting-edge Android assistant designed to bridge the gap between user intent and device action. By leveraging system-level permissions, Autopilot navigates the mobile environment autonomously, executing complex tasks through simple voice commands.

## Features

*   **AI Voice Command Interface:** A centralized, dark-mode home screen featuring a large, animated microphone button that captures user instructions and provides immediate visual feedback.
*   **Accessibility-Driven Automation:** A robust engine that utilizes Android Accessibility Services to perform clicks, scrolls, and cross-app navigation to fulfill user requests.
*   **Live Execution Overlay:** A semi-transparent bottom-sheet interface that narrates the AI's actions in real-time, showing step-by-step logs and "thinking" states without obscuring the background app.
*   **Floating HUD Controller:** A persistent, draggable circular bubble that allows users to pause, stop, or view the AI's reasoning while it operates other applications.
*   **Guided Permission Onboarding:** A visual-centric setup flow designed to clearly explain and facilitate the granting of Accessibility and Screen Capture permissions using diagrams.

## High-Level Tech Stack

*   **Language:** Kotlin
*   **UI Framework:** Jetpack Compose (Material 3)
*   **Asynchronous Programming:** Kotlin Coroutines & Flow
*   **Networking:** Retrofit & OkHttp
*   **Image Loading:** Coil
*   **Code Generation:** KSP (Kotlin Symbol Processing)
*   **Core APIs:** Android Accessibility Service & MediaProjection API (Screen Capture)

## Implementation Steps
**Total Duration:** 17m 1s

### Task_1_Foundation_Onboarding: Initialize the Material 3 theme with a dark-mode-first energetic color scheme, enable Edge-to-Edge display, and implement the visual Permission Setup Flow for Accessibility and Screen Capture permissions using diagrams and clear UI.
- **Status:** COMPLETED
- **Updates:** I have completed **Task 1: Foundation and Onboarding** for the Operon (Autopilot) app.

### Task_2_Assistant_Interface: Develop the Home Screen featuring a pulsing microphone button and a 'Recent Commands' section. Implement the Floating HUD as a draggable circular bubble that expands into a control panel for pause/stop/reasoning.
- **Status:** COMPLETED
- **Updates:** I have completed **Task 2: Assistant Interface** for the Operon (Autopilot) app.

### Task_3_Core_Automation: Implement the AccessibilityService to perform system-level actions (click, scroll, navigate) and the MediaProjection API for screen capture. Integrate a command processing layer (using Retrofit/AI Logic) to translate user intent into actions.
- **Status:** COMPLETED
- **Updates:** I have completed **Task 3: Core Automation** for the Operon (Autopilot) app.

### Task_4_Execution_Feedback_Verification: Create the semi-transparent Live Execution bottom-sheet overlay to show step-by-step logs and 'thinking' animations. Generate an adaptive app icon, perform final UI refinements, and conduct a full Run and Verify step to ensure stability and requirement alignment.
- **Status:** COMPLETED
- **Updates:** The Operon (Autopilot) app has been successfully completed and verified.

### Final Summary:

*   **Task 1: Foundation & Onboarding [COMPLETED]**: Initialized the Material 3 theme with a vibrant green/dark-mode aesthetic. Implemented a visual-centric Permission Flow for Accessibility and Screen Capture permissions using custom diagrams.
*   **Task 2: Assistant Interface [COMPLETED]**: Developed the Home Screen with a pulsing microphone button and a "Recent Commands" section. Built a draggable and expandable Floating HUD for persistent control.
*   **Task 3: Core Automation [COMPLETED]**: Implemented the `AutopilotAccessibilityService` for system-level actions (click/scroll) and the `ScreenCaptureService` for real-time analysis. Integrated the Gemini AI API for reasoning.
*   **Task 4: Execution Feedback & Verification [COMPLETED]**: Created the Live Execution bottom-sheet overlay with step-by-step logs and "thinking" animations. Added a functional red stop button and an adaptive app icon. Verified the entire app for stability and requirement alignment.

### Critic Agent Feedback:
The app passed the final quality check. It is stable, functional, and adheres to the "vibrant and energetic" Material 3 design guidelines. Minor suggestions for future refinement include further polishing of the edge-to-edge display on older API levels and enhancing accessibility labels for certain buttons.

The project is now ready for use. Remember to add your `GEMINI_API_KEY` to `local.properties` as described in the Task 3 update.
- **Acceptance Criteria:**
  - Live Execution overlay shows real-time action logs and animations
  - Floating red stop button is functional
  - Adaptive app icon is implemented and matches branding
  - Final stability check: no crashes, all existing tests pass, and critic_agent verifies alignment with user requirements
- **Duration:** 17m 1s

