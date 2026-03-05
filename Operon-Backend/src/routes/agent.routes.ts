import { Router } from 'express';
import { processStep } from '../controllers/agent.controller';

const router = Router();

router.post('/step', processStep);

export { router as agentRoutes };
