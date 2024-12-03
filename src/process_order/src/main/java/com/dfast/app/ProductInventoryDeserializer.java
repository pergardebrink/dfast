package com.dfast.app;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;

public class ProductInventoryDeserializer implements DeserializationSchema<ProductInventory> {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ProductInventory deserialize(byte[] message) throws IOException {
        return objectMapper.readValue(message, ProductInventory.class);
    }

    @Override
    public boolean isEndOfStream(ProductInventory nextElement) {
        return false;
    }

    @Override
    public TypeInformation<ProductInventory> getProducedType() {
        return TypeInformation.of(ProductInventory.class);
    }
}
