package com.farmosync.inventory.infrastructure.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceitaValidadaEvent {
    private String vendaId;
    private String cpfCliente;
    private String status;
    private String motivoRejeicao;
    private List<ItemVendaEmitidoEvent> itens;
}
