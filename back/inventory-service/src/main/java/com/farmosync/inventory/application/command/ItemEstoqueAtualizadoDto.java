package com.farmosync.inventory.application.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemEstoqueAtualizadoDto {
    private String produtoId;
    private String numeroLote;
    private Integer quantidade;
}
