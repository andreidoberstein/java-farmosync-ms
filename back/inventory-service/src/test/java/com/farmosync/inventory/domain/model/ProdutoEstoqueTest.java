package com.farmosync.inventory.domain.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProdutoEstoqueTest {

    @Test
    public void deveBaixarEstoqueComSucesso() {
        Lote lote = Lote.builder()
                .numero("LOT123")
                .quantidade(50)
                .dataValidade(LocalDate.now().plusMonths(6))
                .build();

        ProdutoEstoque produto = ProdutoEstoque.builder()
                .id("PROD99")
                .nome("Amoxicilina")
                .lotes(new ArrayList<>(List.of(lote)))
                .build();

        produto.baixarEstoque("LOT123", 10, LocalDate.now());

        assertEquals(40, lote.getQuantidade());
    }

    @Test
    public void deveLancarErroAoBaixarLoteVencido() {
        Lote lote = Lote.builder()
                .numero("LOT123")
                .quantidade(50)
                .dataValidade(LocalDate.now().minusDays(1))
                .build();

        ProdutoEstoque produto = ProdutoEstoque.builder()
                .id("PROD99")
                .nome("Amoxicilina")
                .lotes(new ArrayList<>(List.of(lote)))
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            produto.baixarEstoque("LOT123", 10, LocalDate.now());
        });

        assertTrue(exception.getMessage().contains("vencido"));
    }

    @Test
    public void deveLancarErroAoBaixarQuantidadeInsuficiente() {
        Lote lote = Lote.builder()
                .numero("LOT123")
                .quantidade(5)
                .dataValidade(LocalDate.now().plusMonths(6))
                .build();

        ProdutoEstoque produto = ProdutoEstoque.builder()
                .id("PROD99")
                .nome("Amoxicilina")
                .lotes(new ArrayList<>(List.of(lote)))
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            produto.baixarEstoque("LOT123", 10, LocalDate.now());
        });

        assertTrue(exception.getMessage().contains("Saldo insuficiente"));
    }

    @Test
    public void deveLancarErroAoBaixarLoteNaoExistente() {
        ProdutoEstoque produto = ProdutoEstoque.builder()
                .id("PROD99")
                .nome("Amoxicilina")
                .lotes(new ArrayList<>())
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            produto.baixarEstoque("LOT_INEXISTENTE", 10, LocalDate.now());
        });

        assertTrue(exception.getMessage().contains("nao encontrado"));
    }
}
