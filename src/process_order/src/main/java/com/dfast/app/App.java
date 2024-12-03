package com.dfast.app;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.co.RichCoMapFunction;
import org.apache.flink.util.Collector;

import java.util.Properties;


public class App {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        KafkaSource<ProductInventory> source = KafkaSource.<ProductInventory>builder()
            .setBootstrapServers("kafka:9092")
            .setTopics("ProductInventory")
            .setGroupId("inventory-group")
            .setStartingOffsets(OffsetsInitializer.earliest())
            .setValueOnlyDeserializer(new ProductInventoryDeserializer())
            .build();

        KafkaSource<OrderItem> orderSource = KafkaSource.<OrderItem>builder()
            .setBootstrapServers("kafka:9092")
            .setTopics("ProductOrder")
            .setGroupId("order-group")
            .setStartingOffsets(OffsetsInitializer.earliest())
            .setValueOnlyDeserializer(new OrderDeserializer())
            .build();


        DataStream<ProductInventory> inventoryStream = env.fromSource(
            source,
            WatermarkStrategy.noWatermarks(),
            "Inventory Source"
        );

        DataStream<OrderItem> orderStream = env.fromSource(
            orderSource,
            WatermarkStrategy.noWatermarks(),
            "Order Source"
        );

        MapStateDescriptor<String, ProductInventory> inventoryStateDescriptor = new MapStateDescriptor<>(
                "inventoryState",
                Types.STRING,
                Types.POJO(ProductInventory.class)
        );

        KeyedStream<OrderItem, String> keyedOrderStream = orderStream
            .keyBy(OrderItem::getProductId);

        KeyedStream<ProductInventory, String> keyedProductInventoryStream = inventoryStream
            .keyBy(ProductInventory::getProductId);


        keyedProductInventoryStream.connect(keyedOrderStream).map(new RichCoMapFunction<ProductInventory, OrderItem, String>() {
            private transient MapState<String, ProductInventory> inventoryState;

            @Override
            public void open(Configuration parameters) {
                inventoryState = getRuntimeContext().getMapState(inventoryStateDescriptor);
            }

            @Override
            public String map1(ProductInventory inventory) throws Exception {
                String productId = inventory.getProductId();
                System.out.println("Updating inventory for Product Id: " + productId);
                inventoryState.put(productId, inventory);
                return productId;
            }

            @Override
            public String map2(OrderItem orderItem) throws Exception {
                String productId = orderItem.getProductId();
                System.out.println("Trying to get product Id from state: " + productId);
                
                ProductInventory inventory = inventoryState.get(productId);
                if (inventory != null) {
                    int currentQuantity = inventory.getQuantity();
                    int orderQuantity = orderItem.getQuantity();
                    int newQuantity = currentQuantity - orderQuantity;

                    System.out.println("Product Id: " + productId + ", old quantity: " + currentQuantity + ", order quantity: " + orderQuantity + ", new Quantity: " + newQuantity);
                    inventory.setQuantity(newQuantity);
                    inventoryState.put(productId, inventory);
                }

                return productId;
            }
        });

        env.execute("Process Orders");
    }
}
