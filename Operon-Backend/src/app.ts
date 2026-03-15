import express from 'express';
import cors from 'cors';
import helmet from 'helmet';
import morgan from 'morgan';
import dotenv from 'dotenv';
import { agentRoutes } from './routes/agent.routes';

import path from 'path';

dotenv.config();

const app = express();

app.use(helmet({
    contentSecurityPolicy: false, // Disable CSP for simplicity in this experimental app or customize it
}));
app.use(cors());
app.use(express.json({ limit: '50mb' }));
app.use(morgan('combined'));

// Serve static files from the 'public' directory
app.use(express.static(path.join(__dirname, '../public')));

app.get('/health', (req, res) => {
    res.status(200).json({ status: 'healthy', timestamp: new Date().toISOString() });
});

// Root route to serve the landing page
app.get('/', (req, res) => {
    res.sendFile(path.join(__dirname, '../public/index.html'));
});

app.use('/agent', agentRoutes);

const PORT = process.env.PORT || 8080;

app.listen(PORT, () => {
    console.log(`Server listening on port ${PORT}`);
});

export default app;
