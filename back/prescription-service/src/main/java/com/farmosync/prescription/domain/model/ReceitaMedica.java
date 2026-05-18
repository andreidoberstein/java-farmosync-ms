package com.farmosync.prescription.domain.model;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;

@Getter
@Builder
public class ReceitaMedica {
    private final CRM crmMedico;
    private final String nomeMedico;
    private final LocalDate dataEmissao;
    private final String assinaturaDigital;

    public ReceitaMedica(CRM crmMedico, String nomeMedico, LocalDate dataEmissao, String assinaturaDigital) {
        if (crmMedico == null) {
            throw new IllegalArgumentException("CRM do medico e obrigatorio");
        }
        if (nomeMedico == null || nomeMedico.isBlank()) {
            throw new IllegalArgumentException("Nome do medico e obrigatorio");
        }
        if (dataEmissao == null) {
            throw new IllegalArgumentException("Data de emissao da receita e obrigatoria");
        }
        if (dataEmissao.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Data de emissao nao pode ser no futuro");
        }
        if (assinaturaDigital == null || assinaturaDigital.isBlank()) {
            throw new IllegalArgumentException("Assinatura digital da receita e obrigatoria");
        }
        this.crmMedico = crmMedico;
        this.nomeMedico = nomeMedico.trim();
        this.dataEmissao = dataEmissao;
        this.assinaturaDigital = assinaturaDigital.trim();
    }

    public boolean isExpirada(LocalDate dataReferencia) {
        return this.dataEmissao.plusDays(10).isBefore(dataReferencia);
    }
}
