package net.n.example.kafka.draft.partition;

import java.util.Map;
import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;

public class CustomParitioner implements Partitioner {

    @Override
    public void configure(Map<String, ?> configs) {
        // No special configuration
    }

    @Override
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes,
            Cluster cluster) {
        // Get the number of partitions for this topic
        int numPartitions = cluster.partitionCountForTopic(topic);

        if (keyBytes == null) {
            // If no key is provided, always go to partition 0
            return 0;
        }

        // If a key is provided, use its hash to decide the partition
        int hashCode = java.util.Arrays.hashCode(keyBytes);
        // Ensure the resulting partition index is non-negative
        int partition = Math.abs(hashCode) % numPartitions;
        return partition;
    }

    @Override
    public void close() {
        // Nothing to close
    }
}
