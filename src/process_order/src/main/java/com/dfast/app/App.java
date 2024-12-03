package com.dfast.app;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.RichCoMapFunction;

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

        MapStateDescriptor<CompositeKey, ProductInventory> inventoryStateDescriptor = new MapStateDescriptor<>(
            "inventoryState",
            TypeInformation.of(CompositeKey.class),
            Types.POJO(ProductInventory.class)
        );

        KeyedStream<OrderItem, CompositeKey> keyedOrderStream = orderStream
            .keyBy(item -> {
                return new CompositeKey(item.getMerchantId(), item.getProductId());
            });

        KeyedStream<ProductInventory, CompositeKey> keyedProductInventoryStream = inventoryStream
            .keyBy(item -> {
                return new CompositeKey(item.getMerchantId(), item.getProductId());
            });

        keyedProductInventoryStream.connect(keyedOrderStream).map(new RichCoMapFunction<ProductInventory, OrderItem, CompositeKey>() {
            private transient MapState<CompositeKey, ProductInventory> inventoryState;

            @Override
            public void open(Configuration parameters) {
                inventoryState = getRuntimeContext().getMapState(inventoryStateDescriptor);
            }

            @Override
            public CompositeKey map1(ProductInventory inventory) throws Exception {
                CompositeKey key = new CompositeKey(inventory.getMerchantId(), inventory.getProductId());
                System.out.println("Updating inventory for Product Id: " + key.getProductId());
                inventoryState.put(key, inventory);
                return key;
            }

            @Override
            public CompositeKey map2(OrderItem orderItem) throws Exception {
                CompositeKey key = new CompositeKey(orderItem.getMerchantId(), orderItem.getProductId());
               
                ProductInventory inventory = inventoryState.get(key);
                if (inventory != null) {
                    int currentQuantity = inventory.getQuantity();
                    int orderQuantity = orderItem.getQuantity();
                    int newQuantity = currentQuantity - orderQuantity;

                    System.out.println("Product Id: " + key.getProductId() + ", old quantity: " + currentQuantity + ", order quantity: " + orderQuantity + ", new Quantity: " + newQuantity);
                    inventory.setQuantity(newQuantity);
                    inventoryState.put(key, inventory);
                }

                return key;
            }
        });

        env.execute("Process Orders");
    }
}
