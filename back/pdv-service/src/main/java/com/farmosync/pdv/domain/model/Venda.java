package com.farmosync.pdv.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Venda {
    private String id;
    @Builder.Default
    private List<ItemVenda> itens = new ArrayList<>();
    private String cpfCliente;
    private BigDecimal valorTotal;
    private VendaStatus status;
    private LocalDateTime dataCriacao;
    private Receita receita;

    public void calcularValorTotal() {
        if (itens == null || itens.isEmpty()) {
            this.valorTotal = BigDecimal.ZERO;
            return;
        }
        this.valorTotal = itens.stream()
                .map(ItemVenda::getValorItem)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void adicionarItem(ItemVenda item) {
        if (this.itens == null) {
            this.itens = new ArrayList<>();
        }
        this.itens.add(item);
        this.calcularValorTotal();
    }

    public void finalizar() {
        if (this.status == VendaStatus.PENDENTE) {
            this.status = VendaStatus.PROCESSADA;
        }
    }

    public void rejeitar() {
        this.status = VendaStatus.REJEITADA;
    }
}
