package com.farmosync.prescription.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditoriaReceita {
    private String id;
    private String vendaId;
    private String cpfCliente;
    private ReceitaMedica receita;
    private StatusValidacao status;
    private String motivoRejeicao;
    private LocalDateTime dataValidacao;

    public void aprovar() {
        this.status = StatusValidacao.APROVADA;
        this.dataValidacao = LocalDateTime.now();
        this.motivoRejeicao = null;
    }

    public void rejeitar(String motivo) {
        this.status = StatusValidacao.REJEITADA;
        this.dataValidacao = LocalDateTime.now();
        this.motivoRejeicao = motivo;
    }
}
