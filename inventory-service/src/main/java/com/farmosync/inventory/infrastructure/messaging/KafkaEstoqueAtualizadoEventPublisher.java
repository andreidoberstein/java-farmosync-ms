package com.farmosync.inventory.infrastructure.messaging;

import com.farmosync.inventory.application.ports.EstoqueAtualizadoEventPublisher;
import com.farmosync.inventory.infrastructure.messaging.event.EstoqueAtualizadoEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaEstoqueAtualizadoEventPublisher implements EstoqueAtualizadoEventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "estoque-atualizado-topic";

    @Override
    public void publicarEstoqueAtualizado(EstoqueAtualizadoEvent event) {
        kafkaTemplate.send(TOPIC, event.getVendaId(), event);
    }
}
