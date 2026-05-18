package com.farmosync.pdv.infrastructure.repository.document;

import com.farmosync.pdv.domain.model.ItemVenda;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemVendaDocument {
    private String produtoId;
    private String nomeProduto;
    private Integer quantidade;
    private BigDecimal precoUnitario;
    private LoteDocument lote;
    private boolean controlado;

    public static ItemVendaDocument fromDomain(ItemVenda domain) {
        if (domain == null) return null;
        return ItemVendaDocument.builder()
                .produtoId(domain.getProdutoId())
                .nomeProduto(domain.getNomeProduto())
                .quantidade(domain.getQuantidade())
                .precoUnitario(domain.getPrecoUnitario())
                .lote(LoteDocument.fromDomain(domain.getLote()))
                .controlado(domain.isControlado())
                .build();
    }

    public ItemVenda toDomain() {
        return ItemVenda.builder()
                .produtoId(this.produtoId)
                .nomeProduto(this.nomeProduto)
                .quantidade(this.quantidade)
                .precoUnitario(this.precoUnitario)
                .lote(this.lote != null ? this.lote.toDomain() : null)
                .controlado(this.controlado)
                .build();
    }
}
