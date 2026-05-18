package com.farmosync.pdv.infrastructure.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.farmosync.pdv.infrastructure.messaging.event.VendaEvent;
import com.farmosync.pdv.infrastructure.repository.document.OutboxEventDocument;
import com.farmosync.pdv.infrastructure.repository.mongo.MongoOutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
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
                log.debug("Enviando evento do outbox ID: {} para o Kafka no topico: {}.", evento.getId(), TOPIC);
                kafkaTemplate.send(TOPIC, evento.getAggregateId(), vendaEvent).get();

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
    }
}
