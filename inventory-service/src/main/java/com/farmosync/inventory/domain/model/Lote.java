package com.farmosync.inventory.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Lote {
    private String numero;
    private int quantidade;
    private LocalDate dataValidade;

    public void baixar(int qtd, LocalDate dataReferencia) {
        if (dataReferencia.isAfter(this.dataValidade)) {
            throw new IllegalArgumentException("Lote " + this.numero + " esta vencido (Validade: " + this.dataValidade + ").");
        }
        if (this.quantidade < qtd) {
            throw new IllegalArgumentException("Saldo insuficiente no lote " + this.numero + 
                    " (Disponivel: " + this.quantidade + ", Solicitado: " + qtd + ").");
        }
        this.quantidade -= qtd;
    }
}
