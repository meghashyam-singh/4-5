FROM maven:3.9-eclipse-temurin-17-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY . .
RUN mvn clean package

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S roboshop
RUN adduser -S roboshop -G roboshop
COPY --from=builder /app/target/shipping-1.0.jar /app/shipping.jar
RUN chown -R roboshop:roboshop /app
ENV CART_ENDPOINT="cart-cont:8080" \
    DB_HOST="mysql-cont"
USER roboshop
CMD ["java", "-jar", "shipping.jar"]