package com.farmosync.inventory.application.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstoqueAtualizadoDto {
    private String vendaId;
    private String status;
    private String motivoRejeicao;
    private List<ItemEstoqueAtualizadoDto> itens;
}
