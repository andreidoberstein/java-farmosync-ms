package com.farmosync.inventory.infrastructure.messaging;

import com.farmosync.inventory.infrastructure.messaging.event.ItemVendaEmitidoEvent;
import com.farmosync.inventory.infrastructure.messaging.event.ReceitaValidadaEvent;
import com.farmosync.inventory.infrastructure.repository.document.LoteDocument;
import com.farmosync.inventory.infrastructure.repository.document.ProdutoEstoqueDocument;
import com.farmosync.inventory.infrastructure.repository.mongo.MongoIdempotenceKeyRepository;
import com.farmosync.inventory.infrastructure.repository.mongo.MongoProdutoEstoqueRepository;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
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
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
public class BaixarEstoqueConsumerIT {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0");

    @Container
    static KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private MongoProdutoEstoqueRepository produtoRepository;

    @Autowired
    private MongoIdempotenceKeyRepository idempotenceRepository;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
    }

    @BeforeEach
    public void setup() {
        produtoRepository.deleteAll();
        idempotenceRepository.deleteAll();
    }

    @Test
    public void deveBaixarEstoqueComSucessoEEnforceIdempotencia() throws InterruptedException {
        String vendaId = UUID.randomUUID().toString();

        ProdutoEstoqueDocument produto = ProdutoEstoqueDocument.builder()
                .id("PROD_OK")
                .nome("Paracetamol")
                .lotes(List.of(LoteDocument.builder()
                        .numero("LOT_OK")
                        .quantidade(100)
                        .dataValidade(LocalDate.now().plusYears(1))
                        .build()))
                .build();
        produtoRepository.save(produto);

        ItemVendaEmitidoEvent item = ItemVendaEmitidoEvent.builder()
                .produtoId("PROD_OK")
                .nomeProduto("Paracetamol")
                .quantidade(10)
                .precoUnitario(new BigDecimal("10.00"))
                .numeroLote("LOT_OK")
                .dataValidade(LocalDate.now().plusYears(1))
                .controlado(false)
                .build();

        ReceitaValidadaEvent event = ReceitaValidadaEvent.builder()
                .vendaId(vendaId)
                .cpfCliente("98765432100")
                .status("APROVADA")
                .itens(List.of(item))
                .build();

        kafkaTemplate.send("receita-validada-topic", vendaId, event);

        Thread.sleep(3000);

        ProdutoEstoqueDocument atualizado = produtoRepository.findById("PROD_OK").orElseThrow();
        assertEquals(90, atualizado.getLotes().get(0).getQuantidade());

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group-inventory-ok");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList("estoque-atualizado-topic"));
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(10));

            ConsumerRecord<String, String> targetRecord = null;
            for (ConsumerRecord<String, String> record : records) {
                if (vendaId.equals(record.key())) {
                    targetRecord = record;
                    break;
                }
            }

            assertNotNull(targetRecord);
            assertTrue(targetRecord.value().contains("SUCESSO"));
        }

        kafkaTemplate.send("receita-validada-topic", vendaId, event);

        Thread.sleep(1000);

        ProdutoEstoqueDocument posReenvio = produtoRepository.findById("PROD_OK").orElseThrow();
        assertEquals(90, posReenvio.getLotes().get(0).getQuantidade());
    }

    @Test
    public void deveReverterBaixasLocaisSeHouverFalhaDeSaldoConcorrente() throws InterruptedException {
        String vendaId = UUID.randomUUID().toString();

        ProdutoEstoqueDocument prodA = ProdutoEstoqueDocument.builder()
                .id("PROD_A")
                .nome("Dipirona")
                .lotes(List.of(LoteDocument.builder()
                        .numero("LOT_A")
                        .quantidade(50)
                        .dataValidade(LocalDate.now().plusYears(1))
                        .build()))
                .build();

        ProdutoEstoqueDocument prodB = ProdutoEstoqueDocument.builder()
                .id("PROD_B")
                .nome("Ibuprofeno")
                .lotes(List.of(LoteDocument.builder()
                        .numero("LOT_B")
                        .quantidade(5)
                        .dataValidade(LocalDate.now().plusYears(1))
                        .build()))
                .build();

        produtoRepository.saveAll(List.of(prodA, prodB));

        ItemVendaEmitidoEvent itemA = ItemVendaEmitidoEvent.builder()
                .produtoId("PROD_A")
                .nomeProduto("Dipirona")
                .quantidade(10)
                .precoUnitario(new BigDecimal("5.00"))
                .numeroLote("LOT_A")
                .dataValidade(LocalDate.now().plusYears(1))
                .controlado(false)
                .build();

        ItemVendaEmitidoEvent itemB = ItemVendaEmitidoEvent.builder()
                .produtoId("PROD_B")
                .nomeProduto("Ibuprofeno")
                .quantidade(10)
                .precoUnitario(new BigDecimal("12.00"))
                .numeroLote("LOT_B")
                .dataValidade(LocalDate.now().plusYears(1))
                .controlado(false)
                .build();

        ReceitaValidadaEvent event = ReceitaValidadaEvent.builder()
                .vendaId(vendaId)
                .cpfCliente("98765432100")
                .status("APROVADA")
                .itens(List.of(itemA, itemB))
                .build();

        kafkaTemplate.send("receita-validada-topic", vendaId, event);

        Thread.sleep(3000);

        ProdutoEstoqueDocument posProcessamentoA = produtoRepository.findById("PROD_A").orElseThrow();
        assertEquals(50, posProcessamentoA.getLotes().get(0).getQuantidade());

        ProdutoEstoqueDocument posProcessamentoB = produtoRepository.findById("PROD_B").orElseThrow();
        assertEquals(5, posProcessamentoB.getLotes().get(0).getQuantidade());

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group-inventory-revert");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList("estoque-atualizado-topic"));
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(10));

            ConsumerRecord<String, String> targetRecord = null;
            for (ConsumerRecord<String, String> record : records) {
                if (vendaId.equals(record.key())) {
                    targetRecord = record;
                    break;
                }
            }

            assertNotNull(targetRecord);
            assertTrue(targetRecord.value().contains("ERRO"));
            assertTrue(targetRecord.value().contains("Saldo insuficiente"));
        }
    }
}
