package net.n.example.kafka.draft;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.consumer.OffsetCommitCallback;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;

public class ConsumerExample {
    public static void main(String[] args) {
        String bootstrapServers = "localhost:9092";
        String topicName = "my-topic";
        String groupId = "my-consumer-group-2";

        // Set consumer properties
        Properties props = new Properties();
        props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class.getName());
        props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class.getName());

        props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);

        // props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        /**
         * Consistency
         */
        // Common production-like configs
        props.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        // props.setProperty(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "5000");

        /**
         * Reliability
         */
        // If the consumer takes too long to call poll(), Kafka assumes the consumer is no longer
        // alive or functioning properly and triggers a rebalance of the consumer group.
        props.setProperty(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "300000");
        props.setProperty(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, "3000");
        // props.setProperty(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "10000");

        /**
         * Performance
         */
        props.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "500");
        props.setProperty(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, "1");
        props.setProperty(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, "52428800");
        // The fetch request that the consumer sends might involve data from multiple partitions
        // hosted on the same broker/node
        props.setProperty(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, "500");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);

        // Subscribe to the topic
        consumer.subscribe(Collections.singletonList(topicName));

        try {
            while (true) {
                // Poll for new records by partition
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(3000));

                for (ConsumerRecord<String, String> record : records) {
                    System.out.printf(
                            "Consumed record: Key=%s, Value=%s, Partition=%d, Offset=%d%n",
                            record.key(), record.value(), record.partition(), record.offset());
                }
                System.out.println("record size = " + records.count());
                if (records.count() != 0) {
                    commitAsync(consumer);
                }
            }
        } finally {
            // Ensure the consumer's last offsets are committed synchronously before closing
            try {
                consumer.commitSync();
            } finally {
                consumer.close();
            }
        }
    }

    private static void commitSync(KafkaConsumer<String, String> consumer) {
        try {
            consumer.commitSync();
        } catch (Exception e) {
            System.err.println("Commit failed");
            e.printStackTrace();
        }
    }

    private static void commitAsync(KafkaConsumer<String, String> consumer) {
        consumer.commitAsync(new OffsetCommitCallback() {
            @Override
            public void onComplete(Map<TopicPartition, OffsetAndMetadata> offsets,
                    Exception exception) {
                if (offsets != null) {
                    offsets.forEach((partition, offsetAndMetadata) -> {
                        System.out.println("Committed offset: " + partition + " "
                                + offsetAndMetadata.offset());
                    });
                }
                if (exception != null) {
                    System.err.println("Commit failed for offsets: " + exception.getMessage());
                }
            }
        });
    }
}
