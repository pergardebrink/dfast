package com.dfast.app;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.Properties;

public class App {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "kafka:9092");
        properties.setProperty("group.id", "flink-group");

        KafkaSource<ProductInventory> source = KafkaSource.<ProductInventory>builder()
            .setBootstrapServers("kafka:9092")
            .setTopics("ProductInventory")
            .setGroupId("inventory-group")
            .setStartingOffsets(OffsetsInitializer.earliest())
            .setValueOnlyDeserializer(new ProductInventoryDeserializer())
            .build();


        DataStream<ProductInventory> inventoryStream =  env.fromSource(
            source,
            WatermarkStrategy.noWatermarks(),
            "Kafka Source"
        );

        MapStateDescriptor<String, ProductInventory> inventoryStateDescriptor = new MapStateDescriptor<>(
                "inventoryState",
                Types.STRING,
                Types.POJO(ProductInventory.class)
        );

        inventoryStream.keyBy(inventory -> {
                return inventory.getProductId();
        }).map(new RichMapFunction<ProductInventory, String>() {
            private transient MapState<String, ProductInventory> inventoryState;

            @Override
            public void open(Configuration parameters) {
                inventoryState = getRuntimeContext().getMapState(inventoryStateDescriptor);
            }

            @Override
            public String map(ProductInventory inventory) throws Exception {

                inventoryState.put(inventory.getProductId(), inventory);
                System.out.println("Updated Inventory: " + inventory);
                return inventory.getProductId();
            }
        });

        env.execute("Update Inventory State");
    }
}
