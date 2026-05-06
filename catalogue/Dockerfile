FROM node:20-alpine3.22 AS builder
WORKDIR /app
COPY package*.json ./
RUN npm install --only=production
COPY . .

FROM node:20-alpine3.22
WORKDIR /app
RUN addgroup -S roboshop
RUN adduser -S roboshop -G roboshop
COPY --from=builder /app /app
RUN chown -R roboshop:roboshop /app
ENV MONGO="true" \
    MONGO_URL="mongodb://mongodb-cont:27017/catalogue"
USER roboshop
CMD ["node", "server.js"]