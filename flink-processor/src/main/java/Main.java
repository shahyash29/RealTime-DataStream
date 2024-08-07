import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.jdbc.JdbcConnectionOptions;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.JdbcSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.api.common.functions.MapFunction;

import java.util.Objects;

public class Main {

    private static final String BROKERS = "kafka:9092";
    private static final String TOPIC = "weather";
    private static final String GROUP_ID = "groupdId-919292";
    private static final String JDBC_URL = "jdbc:postgresql://docker.for.mac.host.internal:5438/postgres";
    private static final String JDBC_USER = "postgres";
    private static final String JDBC_PASSWORD = "postgres";

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        System.out.println("Environment created");

        KafkaSource<Weather> source = KafkaSource.<Weather>builder()
                .setBootstrapServers(BROKERS)
                .setProperty("partition.discovery.interval.ms", "1000")
                .setTopics(TOPIC)
                .setGroupId(GROUP_ID)
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new WeatherDeserializationSchema())
                .build();

        DataStreamSource<Weather> kafkaStream = env.fromSource(source, WatermarkStrategy.noWatermarks(), "kafka");

        System.out.println("Kafka source created");

        DataStream<Tuple2<MyAverage, Double>> averageTemperatureStream = kafkaStream
                .keyBy(weather -> weather.city)
                .window(TumblingProcessingTimeWindows.of(Time.seconds(60)))
                .aggregate(new AverageAggregator());

        DataStream<Tuple2<String, Double>> cityAndValueStream = averageTemperatureStream
                .map(new MapFunction<Tuple2<MyAverage, Double>, Tuple2<String, Double>>() {
                    @Override
                    public Tuple2<String, Double> map(Tuple2<MyAverage, Double> input) {
                        return new Tuple2<>(input.f0.city, input.f1);
                    }
                });

        System.out.println("Aggregation created");

        cityAndValueStream.addSink(JdbcSink.sink(
                "INSERT INTO weather (city, average_temperature) VALUES (?, ?)",
                (statement, event) -> {
                    statement.setString(1, event.f0);
                    statement.setDouble(2, event.f1);
                },
                JdbcExecutionOptions.builder()
                        .withBatchSize(1000)
                        .withBatchIntervalMs(200)
                        .withMaxRetries(5)
                        .build(),
                new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                        .withUrl(JDBC_URL)
                        .withDriverName("org.postgresql.Driver")
                        .withUsername(JDBC_USER)
                        .withPassword(JDBC_PASSWORD)
                        .build()
        ));

        env.execute("Kafka-flink-postgres");
    }

    public static class AverageAggregator implements AggregateFunction<Weather, MyAverage, Tuple2<MyAverage, Double>> {
        @Override
        public MyAverage createAccumulator() {
            return new MyAverage();
        }

        @Override
        public MyAverage add(Weather weather, MyAverage myAverage) {
            myAverage.city = weather.city;
            myAverage.count++;
            myAverage.sum += weather.temperature;
            return myAverage;
        }

        @Override
        public Tuple2<MyAverage, Double> getResult(MyAverage myAverage) {
            return new Tuple2<>(myAverage, myAverage.sum / myAverage.count);
        }

        @Override
        public MyAverage merge(MyAverage myAverage, MyAverage acc1) {
            myAverage.sum += acc1.sum;
            myAverage.count += acc1.count;
            return myAverage;
        }
    }

    public static class MyAverage {
        public String city;
        public int count = 0;
        public double sum = 0.0;

        @Override
        public String toString() {
            return "MyAverage{" +
                    "city='" + city + '\'' +
                    ", count=" + count +
                    ", sum=" + sum +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MyAverage myAverage = (MyAverage) o;
            return count == myAverage.count &&
                    Double.compare(myAverage.sum, sum) == 0 &&
                    Objects.equals(city, myAverage.city);
        }

        @Override
        public int hashCode() {
            return Objects.hash(city, count, sum);
        }
    }
}
