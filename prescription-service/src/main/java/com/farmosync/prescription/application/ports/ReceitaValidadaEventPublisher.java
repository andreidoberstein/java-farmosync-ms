package com.farmosync.prescription.application.ports;

import com.farmosync.prescription.infrastructure.messaging.event.ReceitaValidadaEvent;

public interface ReceitaValidadaEventPublisher {
    void publicarReceitaValidada(ReceitaValidadaEvent event);
}
