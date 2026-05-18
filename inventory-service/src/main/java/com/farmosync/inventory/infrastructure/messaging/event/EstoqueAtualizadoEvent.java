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
public class EstoqueAtualizadoEvent {
    private String vendaId;
    private String status;
    private String motivoRejeicao;
    private List<ItemEstoqueAtualizadoEvent> itens;
}
