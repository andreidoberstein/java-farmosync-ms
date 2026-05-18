package com.farmosync.inventory.infrastructure.messaging;

import com.farmosync.inventory.application.usecase.BaixarEstoqueUseCase;
import com.farmosync.inventory.infrastructure.messaging.event.ReceitaValidadaEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReceitaValidadaConsumer {
    private final BaixarEstoqueUseCase useCase;

    @KafkaListener(topics = "receita-validada-topic", groupId = "inventory-group")
    public void consumir(ReceitaValidadaEvent event) {
        useCase.processarBaixaEstoque(event);
    }
}
