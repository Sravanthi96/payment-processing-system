# Payment Processing Service

## Overview

The **Payment Processing Service** is an event-driven backend application that simulates a real-world payment gateway. The service consumes **OrderCreated** events from Apache Kafka, processes payments asynchronously, and publishes **PaymentSuccess** or **PaymentFailed** events for downstream services.

The application demonstrates how modern microservices communicate using asynchronous messaging to build scalable, loosely coupled, and fault-tolerant distributed systems.

---

## Features

* Event-driven payment processing
* Consumes **OrderCreated** events from Apache Kafka
* Simulates payment gateway authorization
* Publishes **PaymentSuccess** and **PaymentFailed** events
* Asynchronous communication using Kafka
* REST APIs for payment operations
* Layered architecture using Spring Boot
* Persistent payment records using PostgreSQL
* Centralized exception handling and logging
* Containerized development environment using Docker

---

## Technology Stack

| Technology      | Purpose                  |
| --------------- | ------------------------ |
| Java 25         | Programming Language     |
| Spring Boot     | Backend Framework        |
| Spring Data JPA | Database Access          |
| Hibernate       | ORM                      |
| Apache Kafka    | Event Streaming Platform |
| PostgreSQL      | Database                 |
| Maven           | Build Tool               |
| Docker          | Containerization         |

---

## System Architecture

```text
                   +----------------------+
                   |     Order Service    |
                   +----------------------+
                              |
                              | OrderCreated Event
                              |
                              v
                    +------------------+
                    |   Kafka Topic    |
                    +------------------+
                              |
                              v
          +-------------------------------------------+
          |      Payment Processing Service           |
          |-------------------------------------------|
          | • Consume OrderCreated Event              |
          | • Simulate Payment Gateway                |
          | • Process Payment                         |
          | • Persist Payment in PostgreSQL           |
          | • Publish Payment Result                  |
          +-------------------------------------------+
                    |                       |
                    |                       |
          PaymentSuccess          PaymentFailed
                    |                       |
                    +-----------+-----------+
                                |
                           Kafka Topics
                                |
                                v
                     Downstream Microservices
```

---

## Event Flow

1. The **Order Service** publishes an **OrderCreated** event to Kafka.
2. The Payment Processing Service consumes the event.
3. The service simulates payment authorization.
4. Payment details are persisted in PostgreSQL.
5. A **PaymentSuccess** or **PaymentFailed** event is published to Kafka.
6. Other microservices consume these events to continue the business workflow.

---

## Kafka Topics

### Consumed

| Topic           | Description                               |
| --------------- | ----------------------------------------- |
| `order-created` | Published when a customer places an order |

### Produced

| Topic             | Description                                      |
| ----------------- | ------------------------------------------------ |
| `payment-success` | Published when payment is processed successfully |
| `payment-failed`  | Published when payment processing fails          |

---

## Project Structure

```text
src
 ├── controller
 ├── service
 ├── repository
 ├── entity
 ├── dto
 ├── kafka
 │     ├── consumer
 │     ├── producer
 │     └── events
 ├── config
 ├── exception
 └── util
```

---

## Running the Application

### Prerequisites

* Java 25
* Maven
* Docker

### Start Infrastructure

Start PostgreSQL and Kafka using Docker.

```bash
docker compose up -d
```

### Build the Application

```bash
mvn clean install
```

### Run the Service

```bash
mvn spring-boot:run
```

---

## Design Principles

* Event-Driven Architecture
* Asynchronous Messaging
* Loose Coupling
* Layered Architecture
* Separation of Concerns
* Fault-Tolerant Communication
* Scalable Microservice Design

---

## Future Enhancements

* Idempotent payment processing
* Retry mechanism with exponential backoff
* Dead Letter Queue (DLQ)
* Transactional Outbox Pattern
* OpenAPI/Swagger documentation
* Distributed tracing
* Prometheus and Grafana monitoring
* Kubernetes deployment
* CI/CD pipeline

---

## What This Project Demonstrates

* Java 25 backend development
* Spring Boot microservices
* Event-driven architecture
* Apache Kafka producers and consumers
* Asynchronous payment processing
* PostgreSQL persistence
* Docker-based local development
* Clean architecture and enterprise backend design
* Modern distributed systems development

---

## License

This project was developed as a portfolio project to demonstrate backend engineering, event-driven microservices, and distributed system design using Java 25, Spring Boot, Apache Kafka, PostgreSQL, and Docker.
