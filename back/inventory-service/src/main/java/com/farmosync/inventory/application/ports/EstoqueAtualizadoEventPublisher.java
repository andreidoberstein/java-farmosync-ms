package com.farmosync.inventory.application.ports;

import com.farmosync.inventory.infrastructure.messaging.event.EstoqueAtualizadoEvent;

public interface EstoqueAtualizadoEventPublisher {
    void publicarEstoqueAtualizado(EstoqueAtualizadoEvent event);
}
