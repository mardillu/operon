FROM node:20-alpine AS builder

WORKDIR /app
COPY package*.json ./
RUN npm install

COPY . .
RUN npm run build

FROM node:20-alpine

WORKDIR /app
COPY package*.json ./
RUN npm install --only=production

COPY --from=builder /app/dist ./dist

# Use a non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

ENV PORT=8080
EXPOSE 8080

CMD ["node", "dist/app.js"]
