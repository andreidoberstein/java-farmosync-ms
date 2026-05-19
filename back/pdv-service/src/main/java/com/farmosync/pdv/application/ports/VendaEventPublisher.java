package com.farmosync.pdv.application.ports;

import com.farmosync.pdv.domain.model.Venda;

public interface VendaEventPublisher {
    String obterPayloadVendaEmitida(Venda venda);
}
