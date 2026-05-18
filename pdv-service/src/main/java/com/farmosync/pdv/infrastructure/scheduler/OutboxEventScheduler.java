package com.farmosync.pdv.infrastructure.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.farmosync.pdv.infrastructure.messaging.event.VendaEvent;
import com.farmosync.pdv.infrastructure.repository.document.OutboxEventDocument;
import com.farmosync.pdv.infrastructure.repository.mongo.MongoOutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OutboxEventScheduler {

    private final MongoOutboxEventRepository outboxRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private static final String TOPIC = "venda-emitida-topic";

    @Scheduled(fixedDelay = 1000)
    public void processarEventosOutbox() {
        List<OutboxEventDocument> eventosPendentes = outboxRepository.findByStatusOrderByDataCriacaoAsc("PENDING");

        for (OutboxEventDocument evento : eventosPendentes) {
            try {
                VendaEvent vendaEvent = objectMapper.readValue(evento.getPayload(), VendaEvent.class);
                kafkaTemplate.send(TOPIC, evento.getAggregateId(), vendaEvent).get();

                evento.setStatus("PROCESSED");
                evento.setDataProcessamento(LocalDateTime.now());
                outboxRepository.save(evento);
            } catch (Exception e) {
                evento.setTentativas(evento.getTentativas() + 1);
                if (evento.getTentativas() >= 5) {
                    evento.setStatus("FAILED");
                }
                outboxRepository.save(evento);
            }
        }
    }
}
