import { GoogleGenAI } from '@google/genai';
import { logger } from '../utils/logger';
import dotenv from 'dotenv';

dotenv.config();

export type ActionType = 'click' | 'scroll' | 'input_text' | 'back' | 'home' | 'recent_apps' | 'wait';

export interface ActionTarget {
    text: string | null;
    contentDescription: string | null;
    bounds: [number, number, number, number] | null;
}

export interface StructuredActionResponse {
    goalStatus: 'in_progress' | 'completed' | 'failed';
    reasoning: string;
    nextAction: {
        type: ActionType;
        target: ActionTarget | null;
        inputText: string | null;
        isRisky: boolean;
    };
    confidence: number;
}

const SYSTEM_PROMPT = `You are the "Agent Brain" for an Android UI Automation app.
Your task is to analyze the provided UI tree and/or screenshot and determine the next action to achieve the user's goal.

CRITICAL INSTRUCTIONS:
1. You MUST ALWAYS respond with a VALID JSON object adhering strictly to the required schema. No conversational text whatsoever.
2. The allowed 'type' values for 'nextAction' are strictly: 'click', 'scroll', 'input_text', 'back', 'home', 'recent_apps', 'wait'. Do not hallucinate other actions.
3. Prioritize using the accessibility UI tree data (like text and contentDescription) to identify elements.
4. If a match is found in the UI tree, return its bounds. NEVER guess coordinates; only use exact bounds from the UI tree or reliable detection.
5. IF THE USER ASKS YOU TO OPEN AN APP that isn't on screen, AND you are currently inside the "Operon" application, YOUR VERY FIRST ACTION MUST BE 'home' to escape the app and reach the launcher.
6. When trying to find an app on the Android Launcher, STOP endlessly swiping. Look for a search bar, or swipe up into the App Drawer to find the search bar, then use 'input_text' to search for the app by name. This is much faster.
7. If the goal is met, set goalStatus to 'completed' and nextAction to a 'wait'.
8. Contextual Risk Assessment: You MUST evaluate if your \`click\` action is risky. Set \`isRisky\` to true for ANY click that submits data, deletes a resource, makes a payment, changes important settings, or sends a message. Err on the side of caution: if you are unsure if a click modifies state, set \`isRisky\` to true. Set \`isRisky\` to false ONLY IF it is purely for navigation, media playback ('Play Music'), opening menus, or reading data.

JSON Response Format (Example):
{
  "goalStatus": "in_progress",
  "reasoning": "Found the 'Confirm' button, clicking it to finish booking.",
  "nextAction": {
    "type": "click",
    "target": {
      "text": "Confirm",
      "contentDescription": null,
      "bounds": [100, 200, 300, 250]
    },
    "inputText": null,
    "isRisky": true
  },
  "confidence": 0.95
}`;

export class GeminiService {
    private ai: GoogleGenAI;
    private model: string;

    constructor() {
        const env = (process.env.APP_ENV || 'LIVE').toUpperCase();

        this.model = env === 'DEV' ? 'gemini-2.5-flash' : 'gemini-2.5-pro';

        this.ai = new GoogleGenAI({
            vertexai: true,
            project: process.env.GOOGLE_CLOUD_PROJECT || '',
            location: process.env.GOOGLE_CLOUD_LOCATION || 'us-central1'
        });
    }

    async analyzeScreen(goal: string, uiTree: any, screenshotBase64: string | undefined): Promise<StructuredActionResponse> {
        const promptParts: any[] = [
            `User Goal: ${goal}`
        ];

        if (uiTree) {
            promptParts.push(`UI Tree JSON:\n${JSON.stringify(uiTree)}`);
        }

        if (screenshotBase64) {
            promptParts.push({
                inlineData: {
                    data: screenshotBase64,
                    mimeType: 'image/png' // Assuming PNG for base64
                }
            });
        }

        try {
            const resp = await this.ai.models.generateContent({
                model: this.model,
                contents: promptParts,
                config: {
                    systemInstruction: SYSTEM_PROMPT,
                    responseMimeType: 'application/json',
                    temperature: 0.1,
                }
            });

            const responseText = resp.text;

            if (!responseText) {
                throw new Error('Empty response from Gemini');
            }

            const parsed = JSON.parse(responseText);
            return this.validateResponse(parsed);

        } catch (error) {
            logger.error('Gemini inference failed:', error);
            throw error;
        }
    }

    private validateResponse(data: any): StructuredActionResponse {
        if (!data || typeof data !== 'object') {
            throw new Error('Invalid JSON format returned by Gemini.');
        }

        const allowedActions = ['click', 'scroll', 'input_text', 'back', 'home', 'recent_apps', 'wait'];
        if (data.nextAction && typeof data.nextAction === 'object') {
            if (!allowedActions.includes(data.nextAction.type)) {
                logger.warn(`Invalid action type hallucinated: ${data.nextAction.type}, defaulting to 'wait'`);
                data.nextAction.type = 'wait';
            }
        } else if (data.nextAction) {
            logger.warn(`Invalid nextAction structure, setting to 'wait'`);
            data.nextAction = { type: 'wait', target: null, inputText: null, isRisky: false };
        }

        // Strict Enforcement: If LLM omitted isRisky, default to true for clicks (err on the side of caution)
        if (data.nextAction && typeof data.nextAction.isRisky !== 'boolean') {
            data.nextAction.isRisky = data.nextAction.type === 'click';
        }

        return data as StructuredActionResponse;
    }
}
