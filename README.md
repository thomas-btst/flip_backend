# **Flip Skateshop Backend**

Flip Skateshop Backend is a backend application written in **Kotlin** with the **Spring Boot** framework to manage Flip Skateshop Website.

## **Table of Contents**

1. [Technologies Used](#technologies-used)
2. [Architecture](#architecture)
3. [Getting Started](#getting-started)
4. [Usage](#usage)
5. [Contributing](#contributing)
6. [Author](#author)
7. [License](#license)

## **Technologies Used**

- **Language**: [Kotlin](https://kotlinlang.org/)
- **Framework**: [Spring Boot](https://spring.io/projects/spring-boot)
- **Database**: [MongoDB](https://www.mongodb.com)
- **File Server**: [Minio](https://min.io/)
- **Authentication**: [JWT](https://jwt.io/) (JSON Web Token)
- **Templates**: [Thymeleaf](https://www.thymeleaf.org/)
- **Testing**: [JUnit 5](https://junit.org/junit5/), [Mockk](https://mockk.io/), WebTestClient
- **Build and Dependency Management**: [Maven](https://maven.apache.org/)
- **Others**:
    - WebFlux for reactive endpoints.
    - [Swagger](https://swagger.io/)/[OpenAPI](https://www.openapis.org/) for API documentation.

## **Architecture**

The project follows an **MVC** (Model-View-Controller) architecture :

- **Controller**: Handles HTTP requests and returns responses.
- **Service**: Contains the business logic.
- **Mapper**: The Mapper converts data between database entities and DTO (Data Transfer Object).
- **Repository**: Manages database access.
- **Model**: Represents entities saved in the database.

## **Getting started**

### Requirements

For building and running the application you need :

- **[Java 21](https://www.oracle.com/fr/java/technologies/downloads/#java21)** installed
- **[Docker Compose](https://docs.docker.com/compose/)** installed

### Steps

1. Clone the project :
``` bash
git clone git@etulab.univ-amu.fr:student-id/flip_backend.git
cd flip_backend
```
2. Launch database and file server with Docker Compose :
``` bash
docker compose up -d
```
3. Launch database seeder (Optional) :
``` bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="--seed"
```
4. Start the application with Maven :
``` bash
./mvnw spring-boot:run
```

## **Usage**

#### Run Locally
``` bash
./mvnw spring-boot:run
```
#### Run tests
``` bash
./mvnw test
```
#### Run code quality tool
``` bash
./mvnw ktlint:check
```
### Run database seeder
This tool also creates an admin user with the email `admin@flip.fr` and the password `admin`.
``` bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="--seed"
```
You can also seed specific collections by specifying their names in a comma-separated list. For example, to seed only the `users` and `products` collections, use the following command:
``` bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="--seed=users,products"
```
Supported collections names: users, products, etc.
#### Open API : Swagger UI
Access the Swagger API to http://localhost:8080/swagger-ui.html

## **Contributing**

Before pushing your code, follow these steps to ensure its validity :

1. **Build the project** Make sure the project can build successfully
``` bash
./mvnw clean build
```
2. **Run tests** Execute all tests to verify that your changes don't break any functionality
``` bash
./mvnw clean test
```
3. **Run code quality tool** verify that your code respects kotlin standards
``` bash
./mvnw ktlint:check
```
4. **Check code coverage** (Optional)
``` bash
./mvnw jacoco:report
```
5. **Push Your Changes** You can now push your changes safely
``` bash
git add .
git commit
git push
```

## **Author**

**Name** : Thomas BATISTA  
**Institution** : IUT of Arles  
**Role** : Student developer  
**Program** : BUT Informatique  
**Contact** : developer@example.com  

## **License**

This project is developed as part of my formation.  
It is used for educational purposes only and is not intended for commercial use.