package net.n.example.kafka.draft.serialization;

import java.nio.ByteBuffer;
import java.util.Map;
import org.apache.kafka.common.serialization.Serializer;

public class UserSerializer implements Serializer<User> {

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        // No special configuration
    }

    @Override
    public byte[] serialize(String topic, User data) {
        if (data == null) {
            return null;
        }

        byte[] nameBytes = data.getName().getBytes();
        int sizeOfName = nameBytes.length;

        // We'll store: [int: length of name][name bytes][int: age]
        ByteBuffer buffer = ByteBuffer.allocate(4 + sizeOfName + 4);
        buffer.putInt(sizeOfName);
        buffer.put(nameBytes);
        buffer.putInt(data.getAge());

        return buffer.array();
    }

    @Override
    public void close() {
        // Nothing to close
    }
}
