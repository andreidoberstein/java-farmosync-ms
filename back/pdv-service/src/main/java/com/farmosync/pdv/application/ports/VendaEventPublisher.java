package com.farmosync.pdv.application.ports;

import com.farmosync.pdv.domain.model.Venda;

public interface VendaEventPublisher {
    void publicarVendaEmitida(Venda venda);
}
