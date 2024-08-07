# Flink-Kafka-Postgres Data Pipeline

This project demonstrates a real-time data processing pipeline using Apache Flink, Apache Kafka, and PostgreSQL. The pipeline reads weather data from Kafka, processes it with Flink, and stores the results in a PostgreSQL database.

## Prerequisites

Before you begin, ensure you have the following installed:

- Docker
- Docker Compose
- Java (JDK 8 or higher)
- Maven

## Setup and Running the Project

1. **Clone the repository:**

    ```sh
    git clone https://github.com/shahyash29/RealTime-DataStream.git
    cd RealTime-DataStream
    ```

2. **Build the Flink processor:**

    ```sh
    mvn clean package
    ```

3. **Build Docker images:**

    ```sh
    docker build -t flink-processor .
    ```

4. **Start the services using Docker Compose:**

    ```sh
    docker-compose up -d
    ```

5. **Start the data generator:**

    ```sh
    python python-producer.py
    ```

## Project Structure

- **Main.java**: Configures and runs the Flink job to process data from Kafka and write to PostgreSQL.
- **Weather.java**: Defines the weather data structure.
- **WeatherDeserializationSchema.java**: Handles the deserialization of weather data from Kafka.
- **data_generator.py**: Generates and sends synthetic weather data to Kafka.
- **docker-compose.yml**: Defines the multi-container Docker application setup for Kafka, Zookeeper, Flink, and PostgreSQL.
- **Dockerfile**: Builds the Docker image for the Flink processor.

## Docker Commands

- **Access PostgreSQL container:**

    ```sh
    docker exec -it postgres-container psql -U postgres -d mydatabase
    ```

