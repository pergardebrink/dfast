package com.dfast.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;

public class OrderDeserializer implements DeserializationSchema<OrderItem> {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public OrderItem deserialize(byte[] message) throws IOException {
        return objectMapper.readValue(message, OrderItem.class);
    }

    @Override
    public boolean isEndOfStream(OrderItem nextElement) {
        return false;
    }

    @Override
    public TypeInformation<OrderItem> getProducedType() {
        return TypeInformation.of(OrderItem.class);
    }
}
