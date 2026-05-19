package com.farmosync.pdv.infrastructure.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstoqueAtualizadoEvent {
    private String vendaId;
    private String status;
    private String motivoRejeicao;
}
