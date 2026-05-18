package com.farmosync.prescription.infrastructure.messaging;

import com.farmosync.prescription.infrastructure.messaging.event.ItemVendaEmitidoEvent;
import com.farmosync.prescription.infrastructure.messaging.event.ReceitaEmitidaEvent;
import com.farmosync.prescription.infrastructure.messaging.event.VendaEmitidaEvent;
import com.farmosync.prescription.infrastructure.repository.document.AuditoriaReceitaDocument;
import com.farmosync.prescription.infrastructure.repository.mongo.MongoAuditoriaReceitaRepository;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
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
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
public class ValidarReceitaConsumerIT {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0");

    @Container
    static KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private MongoAuditoriaReceitaRepository mongoRepository;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
    }

    @Test
    public void deveAprovarReceitaValidaEGravarAuditoria() throws InterruptedException {
        String vendaId = UUID.randomUUID().toString();

        ItemVendaEmitidoEvent item = ItemVendaEmitidoEvent.builder()
                .produtoId("PROD1")
                .nomeProduto("Amoxicilina")
                .quantidade(2)
                .precoUnitario(new BigDecimal("25.00"))
                .numeroLote("LOT1")
                .dataValidade(LocalDate.now().plusYears(1))
                .controlado(true)
                .build();

        ReceitaEmitidaEvent receita = ReceitaEmitidaEvent.builder()
                .crmMedico("12345")
                .crmUf("SP")
                .nomeMedico("Dr. Drauzio Varella")
                .dataEmissao(LocalDate.now().minusDays(2))
                .assinaturaDigital("ASSINATURA123")
                .build();

        VendaEmitidaEvent event = VendaEmitidaEvent.builder()
                .vendaId(vendaId)
                .cpfCliente("12345678909")
                .valorTotal(new BigDecimal("50.00"))
                .dataCriacao(LocalDateTime.now())
                .itens(List.of(item))
                .receita(receita)
                .build();

        kafkaTemplate.send("venda-emitida-topic", vendaId, event);

        Thread.sleep(3000);

        assertTrue(mongoRepository.findByVendaId(vendaId).isPresent());
        AuditoriaReceitaDocument auditoria = mongoRepository.findByVendaId(vendaId).orElseThrow();
        assertEquals("APROVADA", auditoria.getStatus());
        assertNull(auditoria.getMotivoRejeicao());

        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group-prescription-approved");
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties)) {
            consumer.subscribe(Collections.singletonList("receita-validada-topic"));
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(10));
            
            ConsumerRecord<String, String> targetRecord = null;
            for (ConsumerRecord<String, String> record : records) {
                if (vendaId.equals(record.key())) {
                    targetRecord = record;
                    break;
                }
            }
            
            assertNotNull(targetRecord);
            assertTrue(targetRecord.value().contains("APROVADA"));
            assertTrue(targetRecord.value().contains("Amoxicilina"));
        }
    }

    @Test
    public void deveRejeitarReceitaExpiradaEGravarAuditoria() throws InterruptedException {
        String vendaId = UUID.randomUUID().toString();

        ItemVendaEmitidoEvent item = ItemVendaEmitidoEvent.builder()
                .produtoId("PROD1")
                .nomeProduto("Amoxicilina")
                .quantidade(2)
                .precoUnitario(new BigDecimal("25.00"))
                .numeroLote("LOT1")
                .dataValidade(LocalDate.now().plusYears(1))
                .controlado(true)
                .build();

        ReceitaEmitidaEvent receita = ReceitaEmitidaEvent.builder()
                .crmMedico("12345")
                .crmUf("SP")
                .nomeMedico("Dr. Drauzio Varella")
                .dataEmissao(LocalDate.now().minusDays(11))
                .assinaturaDigital("ASSINATURA123")
                .build();

        VendaEmitidaEvent event = VendaEmitidaEvent.builder()
                .vendaId(vendaId)
                .cpfCliente("12345678909")
                .valorTotal(new BigDecimal("50.00"))
                .dataCriacao(LocalDateTime.now())
                .itens(List.of(item))
                .receita(receita)
                .build();

        kafkaTemplate.send("venda-emitida-topic", vendaId, event);

        Thread.sleep(3000);

        assertTrue(mongoRepository.findByVendaId(vendaId).isPresent());
        AuditoriaReceitaDocument auditoria = mongoRepository.findByVendaId(vendaId).orElseThrow();
        assertEquals("REJEITADA", auditoria.getStatus());
        assertNotNull(auditoria.getMotivoRejeicao());
        assertTrue(auditoria.getMotivoRejeicao().contains("expirada"));

        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group-prescription-rejected");
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties)) {
            consumer.subscribe(Collections.singletonList("receita-validada-topic"));
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(10));
            
            ConsumerRecord<String, String> targetRecord = null;
            for (ConsumerRecord<String, String> record : records) {
                if (vendaId.equals(record.key())) {
                    targetRecord = record;
                    break;
                }
            }
            
            assertNotNull(targetRecord);
            assertTrue(targetRecord.value().contains("REJEITADA"));
            assertTrue(targetRecord.value().contains("expirada"));
        }
    }
}
