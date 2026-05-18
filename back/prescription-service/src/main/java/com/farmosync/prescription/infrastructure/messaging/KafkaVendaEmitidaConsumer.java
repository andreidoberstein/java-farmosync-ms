package com.farmosync.prescription.infrastructure.messaging;

import com.farmosync.prescription.application.usecase.ValidarReceitaUseCase;
import com.farmosync.prescription.infrastructure.messaging.event.VendaEmitidaEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaVendaEmitidaConsumer {
    private final ValidarReceitaUseCase useCase;

    @KafkaListener(topics = "venda-emitida-topic", groupId = "prescription-group")
    public void consumir(VendaEmitidaEvent event) {
        log.info("Consumido evento de venda emitida do Kafka para a Venda ID: {}.", event.getVendaId());
        try {
            useCase.processarValidacao(event);
            log.info("Evento de venda emitida processado com sucesso para a Venda ID: {}.", event.getVendaId());
        } catch (Exception e) {
            log.error("Erro ao processar validacao de receita para a Venda ID: {}. Causa: {}", event.getVendaId(), e.getMessage(), e);
            throw e;
        }
    }
}
