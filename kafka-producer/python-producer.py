import time
import random
import schedule
from json import dumps
from faker import Faker
from kafka import KafkaProducer

# Define Kafka connection details
KAFKA_NODES = "kafka:9092"
TOPIC = "weather"

# Function to generate and send data
def generate_and_send_data(producer, faker_instance):
    weather_data = {
        'city': faker_instance.city(),
        'temperature': random.uniform(10.0, 110.0)
    }
    print(weather_data)
    producer.send(topic=TOPIC, value=weather_data)
    producer.flush()

# Function to set up Kafka producer
def setup_kafka_producer(kafka_nodes):
    return KafkaProducer(
        bootstrap_servers=kafka_nodes,
        value_serializer=lambda x: dumps(x).encode('utf-8')
    )

# Main execution
def main():
    faker_instance = Faker()
    producer = setup_kafka_producer(KAFKA_NODES)
    
    generate_and_send_data(producer, faker_instance)  # Generate initial data

    # Schedule data generation every 10 seconds
    schedule.every(10).seconds.do(generate_and_send_data, producer, faker_instance)

    # Run scheduled tasks
    while True:
        schedule.run_pending()
        time.sleep(0.5)

if __name__ == "__main__":
    main()
