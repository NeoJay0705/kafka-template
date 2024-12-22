package net.n.example.kafka.draft;

import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

public class ProducerExample {
    public static void main(String[] args) {
        String bootstrapServers = "localhost:9092";
        String topicName = "logs-topic";

        // Set producer properties
        Properties props = new Properties();
        props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.setProperty("log4j.logger.org.apache.kafka.clients.producer", "DEBUG");

        props.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringSerializer");
        props.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringSerializer");
        // Reliability & Delivery
        // Acks control durability guarantees. "all" = leader + replicas ack.
        props.setProperty(ProducerConfig.ACKS_CONFIG, "all"); // Default: "0 | 1 | all"
        // The number of retries. Producer will retry sending if it encounters errors.
        props.setProperty(ProducerConfig.RETRIES_CONFIG, "3");
        // Delivery timeout: maximum time to send a record successfully. It includes retries and the
        // waiting time in buffers.
        props.setProperty(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, "120000");
        // Request timeout for the broker response
        props.setProperty(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, "30000");
        props.setProperty(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "1"); // Ensures
                                                                                      // ordering if
                                                                                      // retries
                                                                                      // occur
                                                                                      // (default:
                                                                                      // 5)
        // Performance & Throughput
        // (topic1-partition0) -> [batch of records]
        // (topic1-partition1) -> [batch of records]
        // (topic2-partition0) -> [batch of records]
        props.setProperty(ProducerConfig.BATCH_SIZE_CONFIG, "16384"); // 16KB
        // Time to wait before sending a batch even if not full.
        props.setProperty(ProducerConfig.LINGER_MS_CONFIG, "5");
        props.setProperty(ProducerConfig.BUFFER_MEMORY_CONFIG, "67108864"); // 64 MB, default: 32MB
        props.setProperty(ProducerConfig.COMPRESSION_TYPE_CONFIG, "gzip"); // Default: none, gzip,
                                                                           // snappy, lz4, zstd
        // Blocking & Timeouts
        // Max time to block send() calls if buffers are full or metadata unavailable
        props.setProperty(ProducerConfig.MAX_BLOCK_MS_CONFIG, "10000");

        KafkaProducer<String, String> producer = new KafkaProducer<>(props);


        try {
            for (int i = 1; i <= 1; i++) {
                String key = "Key-" + (int) (Math.random() * 100);
                String value = "Message-" + System.currentTimeMillis();

                // Send a record
                ProducerRecord<String, String> record = new ProducerRecord<>(topicName, key, value);

                // Asynchronous send with a callback
                // send() through NIO
                // a single background I/O thread to execute the callback
                producer.send(record, (RecordMetadata metadata, Exception exception) -> {
                    if (exception == null) {
                        System.out.printf(
                                "Produced record: Key=%s, Value=%s, Partition=%d, Offset=%d%n",
                                record.key(), record.value(), metadata.partition(),
                                metadata.offset());
                    } else {
                        exception.printStackTrace();
                    }
                });
            }
        } finally {
            producer.close();
        }
    }
}
