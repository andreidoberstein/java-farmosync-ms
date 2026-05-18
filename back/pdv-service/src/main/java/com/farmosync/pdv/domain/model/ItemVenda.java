package com.farmosync.pdv.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemVenda {
    private String produtoId;
    private String nomeProduto;
    private Integer quantidade;
    private BigDecimal precoUnitario;
    private Lote lote;
    private boolean controlado;

    public BigDecimal getValorItem() {
        return precoUnitario.multiply(BigDecimal.valueOf(quantidade));
    }
}
