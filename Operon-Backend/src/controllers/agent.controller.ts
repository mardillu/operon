import { Request, Response } from 'express';
import { AgentService } from '../services/agent.service';
import { logger } from '../utils/logger';

export const processStep = async (req: Request, res: Response): Promise<any> => {
    try {
        const { sessionId, goal, screenshotBase64, uiTree } = req.body;

        if (!goal || (!screenshotBase64 && !uiTree)) {
            return res.status(400).json({ error: 'Missing required parameters. Must provide goal and at least one of screenshotBase64 or uiTree.' });
        }

        const agentService = new AgentService();
        const result = await agentService.determineNextAction({
            sessionId: sessionId || 'anonymous',
            goal,
            screenshotBase64,
            uiTree
        });

        return res.status(200).json(result);
    } catch (error: any) {
        logger.error('Error processing agent step:', error);
        return res.status(500).json({ error: 'Internal server error processing agent step.', details: error.message });
    }
};
