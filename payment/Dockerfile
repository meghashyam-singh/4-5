FROM python:3.11-alpine3.22 AS builder
RUN apk update
RUN apk add --no-cache musl-dev
RUN apk add --no-cache libffi-dev
RUN apk add --no-cache openssl-dev
RUN apk add --no-cache gcc
RUN apk add --no-cache python3-dev
RUN apk add --no-cache uwsgi uwsgi-python3
WORKDIR /app
COPY requirements.txt .
COPY . .
RUN pip3 install --prefix=/install -r requirements.txt

FROM python:3.11-alpine3.22
WORKDIR /app
RUN addgroup -S roboshop
RUN adduser -S roboshop -G roboshop
COPY --from=builder /app /app
COPY --from=builder /install /usr/local
RUN chown -R roboshop:roboshop /app
ENV CART_HOST="cart-cont" \
    CART_PORT="8080" \
    USER_HOST="user-cont" \
    USER_PORT="8080" \
    AMQP_HOST="rabbitmq-cont" \
    AMQP_USER="roboshop" \
    AMQP_PASS="roboshop123"
USER roboshop
CMD ["uwsgi", "--ini", "payment.ini"]