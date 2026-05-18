package com.farmosync.pdv.presentation;

import com.farmosync.pdv.application.dto.ItemRequest;
import com.farmosync.pdv.application.dto.RegistrarVendaRequest;
import com.farmosync.pdv.application.dto.VendaResponse;
import com.farmosync.pdv.infrastructure.repository.document.VendaDocument;
import com.farmosync.pdv.infrastructure.repository.mongo.MongoVendaRepository;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class RegistrarVendaControllerIT {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0");

    @Container
    static KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MongoVendaRepository mongoVendaRepository;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
    }

    @Test
    public void deveRegistrarVendaPersistirNoMongoEPublicarNoKafka() {
        ItemRequest item = ItemRequest.builder()
                .produtoId("PROD1")
                .nomeProduto("Amoxicilina")
                .quantidade(2)
                .precoUnitario(new BigDecimal("25.00"))
                .numeroLote("LOT1")
                .dataValidade(LocalDate.now().plusYears(1))
                .controlado(true)
                .build();

        RegistrarVendaRequest request = RegistrarVendaRequest.builder()
                .cpfCliente("12345678909")
                .itens(List.of(item))
                .build();

        ResponseEntity<VendaResponse> response = restTemplate.postForEntity("/api/v1/vendas", request, VendaResponse.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        VendaResponse body = response.getBody();
        assertNotNull(body.getId());
        assertEquals(new BigDecimal("50.00"), body.getValorTotal());
        assertEquals("PENDENTE", body.getStatus());

        assertTrue(mongoVendaRepository.existsById(body.getId()));
        VendaDocument savedDocument = mongoVendaRepository.findById(body.getId()).orElseThrow();
        assertEquals("12345678909", savedDocument.getCpfCliente());

        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties)) {
            consumer.subscribe(Collections.singletonList("venda-emitida-topic"));
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(10));
            assertEquals(1, records.count());
            ConsumerRecord<String, String> record = records.iterator().next();
            assertEquals(body.getId(), record.key());
            assertTrue(record.value().contains("PROD1"));
            assertTrue(record.value().contains("Amoxicilina"));
        }
    }
}
