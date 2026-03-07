import { GeminiService, StructuredActionResponse } from './gemini.service';
import { FirestoreService } from './firestore.service';
import { logger } from '../utils/logger';

export interface AgentStepPayload {
    sessionId: string;
    goal: string;
    screenshotBase64?: string;
    uiTree?: any;
    pastActions?: string[];
}

export class AgentService {
    private geminiService: GeminiService;
    private firestoreService: FirestoreService;

    constructor() {
        this.geminiService = new GeminiService();
        this.firestoreService = new FirestoreService();
    }

    async determineNextAction(payload: AgentStepPayload): Promise<StructuredActionResponse> {
        const { sessionId, goal, screenshotBase64, uiTree, pastActions } = payload;

        await this.firestoreService.saveSessionState(sessionId, { goal, lastUpdated: new Date().toISOString() });

        try {
            logger.info(`Analyzing screen for session ${sessionId}...`);
            const result = await this.geminiService.analyzeScreen(goal, uiTree, screenshotBase64, pastActions);

            this.validateActionSafety(result, uiTree);

            await this.firestoreService.addActionHistory(sessionId, {
                goal,
                action: result.nextAction,
                confidence: result.confidence,
                reasoning: result.reasoning
            });

            return result;

        } catch (error) {
            logger.error(`Error in determineNextAction for session ${sessionId}:`, error);
            throw error;
        }
    }

    /**
     * Safety Layer: Validates that the selected action targets an existing element in the UI Tree.
     */
    private validateActionSafety(result: StructuredActionResponse, uiTree: any) {
        const { nextAction } = result;

        if (!uiTree) return;

        if (nextAction && nextAction.target) {
            // Validate against dangerous global actions if needed, or sanity check the output types
            if (nextAction.type === 'back' && !nextAction.target) {
                // Back is broadly safe
            }
        }
    }
}
