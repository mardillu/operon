import { Firestore } from '@google-cloud/firestore';
import { logger } from '../utils/logger';
import dotenv from 'dotenv';

dotenv.config();

export class FirestoreService {
    private firestore: Firestore | null = null;
    private enabled: boolean;

    constructor() {
        this.enabled = process.env.FIRESTORE_ENABLED === 'true';
        if (this.enabled) {
            try {
                this.firestore = new Firestore({
                    projectId: process.env.GOOGLE_CLOUD_PROJECT,
                });
                logger.info('Firestore client initialized successfully.');
            } catch (error) {
                logger.error('Failed to initialize Firestore client:', error);
                this.enabled = false;
            }
        } else {
            logger.info('Firestore is disabled via environment variables.');
        }
    }

    async saveSessionState(sessionId: string, data: any) {
        if (!this.enabled || !this.firestore) return;

        try {
            const docRef = this.firestore.collection('sessions').doc(sessionId);
            await docRef.set({
                ...data,
                updatedAt: new Date().toISOString()
            }, { merge: true });
        } catch (error) {
            logger.error(`Failed to save state for session ${sessionId}:`, error);
        }
    }

    async addActionHistory(sessionId: string, action: any) {
        if (!this.enabled || !this.firestore) return;

        try {
            const collectionRef = this.firestore.collection('sessions').doc(sessionId).collection('history');
            await collectionRef.add({
                ...action,
                timestamp: new Date().toISOString()
            });
        } catch (error) {
            logger.error(`Failed to add history for session ${sessionId}:`, error);
        }
    }
}
