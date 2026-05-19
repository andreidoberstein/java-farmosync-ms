package com.farmosync.pdv.infrastructure.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.farmosync.pdv.infrastructure.messaging.event.VendaEvent;
import com.farmosync.pdv.infrastructure.repository.document.OutboxEventDocument;
import com.farmosync.pdv.infrastructure.repository.mongo.MongoOutboxEventRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class OutboxEventScheduler {

    private final MongoOutboxEventRepository outboxRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String topic;
    private final Timer processingTimer;

    public OutboxEventScheduler(
            MongoOutboxEventRepository outboxRepository,
            KafkaTemplate<String, Object> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${app.kafka.topics.venda-emitida}") String topic,
            MeterRegistry meterRegistry) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.topic = topic;

        Gauge.builder("farmosync.outbox.pending.count", outboxRepository,
                repo -> repo.countByStatus("PENDING"))
                .description("Quantidade de eventos pendentes no Outbox")
                .register(meterRegistry);

        this.processingTimer = Timer.builder("farmosync.outbox.processing.duration")
                .description("Tempo gasto processando os eventos do Outbox")
                .register(meterRegistry);
    }

    @Scheduled(fixedDelay = 1000)
    public void processarEventosOutbox() {
        processingTimer.record(() -> {
            PageRequest pageRequest = PageRequest.of(0, 100, Sort.by("dataCriacao").ascending());
            List<OutboxEventDocument> eventosPendentes = outboxRepository.findByStatus("PENDING", pageRequest);

            if (eventosPendentes.isEmpty()) {
                return;
            }

            for (OutboxEventDocument evento : eventosPendentes) {
                try {
                    VendaEvent vendaEvent = objectMapper.readValue(evento.getPayload(), VendaEvent.class);
                    log.debug("Enviando evento do outbox ID: {} para o Kafka no topico: {}.", evento.getId(), topic);
                    kafkaTemplate.send(topic, evento.getAggregateId(), vendaEvent).get(5, TimeUnit.SECONDS);

                    evento.setStatus("PROCESSED");
                    evento.setDataProcessamento(LocalDateTime.now());
                    outboxRepository.save(evento);
                    log.info("Evento do outbox ID: {} (Venda: {}) processado e enviado com sucesso ao Kafka.",
                            evento.getId(), evento.getAggregateId());
                } catch (Exception e) {
                    evento.setTentativas(evento.getTentativas() + 1);
                    if (evento.getTentativas() >= 5) {
                        evento.setStatus("FAILED");
                        log.error("FALHA DEFINITIVA: Evento do outbox ID: {} (Venda: {}) falhou apos atingir o limite de {} tentativas.",
                                evento.getId(), evento.getAggregateId(), evento.getTentativas(), e);
                    } else {
                        log.warn("Tentativa {}/5 falhou para o evento do outbox ID: {} (Venda: {}). Aguardando retry. Causa: {}",
                                evento.getTentativas(), evento.getId(), evento.getAggregateId(), e.getMessage());
                    }
                    outboxRepository.save(evento);
                }
            }
        });
    }
}
