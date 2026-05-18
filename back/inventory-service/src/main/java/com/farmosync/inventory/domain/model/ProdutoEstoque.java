package com.farmosync.inventory.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProdutoEstoque {
    private String id;
    private String nome;
    @Builder.Default
    private List<Lote> lotes = new ArrayList<>();

    public void baixarEstoque(String numeroLote, int quantidade, LocalDate dataReferencia) {
        Lote lote = this.lotes.stream()
                .filter(l -> l.getNumero().equals(numeroLote))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Lote " + numeroLote + 
                        " nao encontrado para o produto " + this.id + " (" + this.nome + ")."));

        lote.baixar(quantidade, dataReferencia);
    }
}
