package com.farmosync.inventory.application.ports;

import com.farmosync.inventory.application.command.EstoqueAtualizadoDto;

public interface EstoqueAtualizadoEventPublisher {
    void publicarEstoqueAtualizado(EstoqueAtualizadoDto event);
}
