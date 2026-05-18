package com.farmosync.inventory.infrastructure.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemEstoqueAtualizadoEvent {
    private String produtoId;
    private String numeroLote;
    private Integer quantidade;
}
