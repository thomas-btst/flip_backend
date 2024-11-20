FROM maven:3.9.9-eclipse-temurin-21

WORKDIR /app

COPY . .

RUN mvn dependency:go-offline
RUN apt-get update && apt-get install -y python3