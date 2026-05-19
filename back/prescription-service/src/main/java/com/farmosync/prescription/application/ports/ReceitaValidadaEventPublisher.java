package com.farmosync.prescription.application.ports;

import com.farmosync.prescription.application.command.ReceitaValidadaDto;

public interface ReceitaValidadaEventPublisher {
    void publicarReceitaValidada(ReceitaValidadaDto resultado);
}
