package com.farmosync.inventory.infrastructure.messaging;

import com.farmosync.inventory.application.usecase.BaixarEstoqueUseCase;
import com.farmosync.inventory.infrastructure.messaging.event.ReceitaValidadaEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReceitaValidadaConsumer {
    private final BaixarEstoqueUseCase useCase;

    @KafkaListener(topics = "receita-validada-topic", groupId = "inventory-group")
    public void consumir(ReceitaValidadaEvent event) {
        log.info("Consumido evento de receita validada do Kafka para a Venda ID: {}. Status da receita: {}.", 
                event.getVendaId(), event.getStatus());
        try {
            useCase.processarBaixaEstoque(event);
            log.info("Evento de receita validada processado com sucesso para a Venda ID: {}.", event.getVendaId());
        } catch (Exception e) {
            log.error("Erro ao processar baixa de estoque para a Venda ID: {}. Causa: {}", event.getVendaId(), e.getMessage(), e);
            throw e;
        }
    }
}
