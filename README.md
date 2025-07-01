# Payment Microservice

A Spring Boot microservice for handling payment processing using Stripe integration.

![Screenshot 2025-07-01 222820.png](images/Screenshot%202025-07-01%20222820.png)
## Overview

This microservice provides payment processing capabilities for invention packages using Stripe as the payment gateway. It handles payment sessions, success/failure redirects, and email notifications.

## Features

- Stripe payment integration
- Email notifications
- Payment status tracking
- Cross-origin resource sharing (CORS) configuration
- Observability with Prometheus and Loki

## Technologies

- Java 17
- Spring Boot 3.4.3
- MySQL Database
- Stripe API
- Spring Mail
- Docker

## Setup and Configuration

### Prerequisites

- JDK 17
- Maven
- MySQL Database
- Stripe Account

### Environment Configuration

Key configurations in `application.properties`:

- Database connection
- Stripe API keys
- Mail server settings
- Frontend URL for CORS

### Building the Application

```bash
./mvnw clean package
```

### Running the Application

```bash
./mvnw spring-boot:run
```

### Docker Deployment

```bash
docker build -t payment-microservice .
docker run -p 5003:5003 payment-microservice
```

## API Endpoints

- `POST /api/payments/create-session` - Create a payment session
- `GET /api/payments/success` - Handle successful payments
- `GET /api/payments/cancel` - Handle canceled payments

## Monitoring

The service includes:
- Prometheus metrics
- Loki logging integration
- Micrometer for metrics collection

## License

[Add your license information here]