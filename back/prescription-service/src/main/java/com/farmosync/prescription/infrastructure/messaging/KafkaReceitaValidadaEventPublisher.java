package com.farmosync.prescription.infrastructure.messaging;

import com.farmosync.prescription.application.ports.ReceitaValidadaEventPublisher;
import com.farmosync.prescription.infrastructure.messaging.event.ReceitaValidadaEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaReceitaValidadaEventPublisher implements ReceitaValidadaEventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "receita-validada-topic";

    @Override
    public void publicarReceitaValidada(ReceitaValidadaEvent event) {
        kafkaTemplate.send(TOPIC, event.getVendaId(), event);
    }
}
