package com.farmosync.prescription.infrastructure.messaging;

import com.farmosync.prescription.application.usecase.ValidarReceitaUseCase;
import com.farmosync.prescription.infrastructure.messaging.event.VendaEmitidaEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaVendaEmitidaConsumer {
    private final ValidarReceitaUseCase useCase;

    @KafkaListener(topics = "venda-emitida-topic", groupId = "prescription-group")
    public void consumir(VendaEmitidaEvent event) {
        useCase.processarValidacao(event);
    }
}
