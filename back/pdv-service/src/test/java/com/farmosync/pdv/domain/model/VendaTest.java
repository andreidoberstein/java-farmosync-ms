package com.farmosync.pdv.domain.model;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class VendaTest {

    @Test
    public void deveCalcularValorTotalComoZeroQuandoNaoHouverItens() {
        Venda venda = Venda.builder().build();
        venda.calcularValorTotal();
        assertEquals(BigDecimal.ZERO, venda.getValorTotal());
    }

    @Test
    public void deveCalcularValorTotalCorretoComMultiplosItens() {
        Lote lote = Lote.builder()
                .numeroLote("L1")
                .dataValidade(LocalDate.now().plusYears(1))
                .build();

        ItemVenda item1 = ItemVenda.builder()
                .produtoId("P1")
                .nomeProduto("Dipirona")
                .quantidade(2)
                .precoUnitario(new BigDecimal("10.00"))
                .lote(lote)
                .controlado(true)
                .build();

        ItemVenda item2 = ItemVenda.builder()
                .produtoId("P2")
                .nomeProduto("Shampoo")
                .quantidade(1)
                .precoUnitario(new BigDecimal("15.50"))
                .controlado(false)
                .build();

        Venda venda = Venda.builder()
                .itens(List.of(item1, item2))
                .build();

        venda.calcularValorTotal();

        assertEquals(new BigDecimal("35.50"), venda.getValorTotal());
    }

    @Test
    public void deveAlterarStatusParaProcessadaAoFinalizar() {
        Venda venda = Venda.builder()
                .status(VendaStatus.PENDENTE)
                .build();

        venda.finalizar();

        assertEquals(VendaStatus.PROCESSADA, venda.getStatus());
    }

    @Test
    public void deveAlterarStatusParaRejeitadaAoRejeitar() {
        Venda venda = Venda.builder()
                .status(VendaStatus.PENDENTE)
                .build();

        venda.rejeitar();

        assertEquals(VendaStatus.REJEITADA, venda.getStatus());
    }

    @Test
    public void deveAdicionarItemERecalcularTotal() {
        Venda venda = Venda.builder().build();
        ItemVenda item = ItemVenda.builder()
                .produtoId("P1")
                .nomeProduto("Dipirona")
                .quantidade(2)
                .precoUnitario(new BigDecimal("10.00"))
                .controlado(false)
                .build();

        venda.adicionarItem(item);

        assertEquals(1, venda.getItens().size());
        assertEquals(new BigDecimal("20.00"), venda.getValorTotal());
    }
}
