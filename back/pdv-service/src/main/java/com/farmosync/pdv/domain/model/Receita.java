package com.farmosync.pdv.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Receita {
    private String crmMedico;
    private String crmUf;
    private String nomeMedico;
    private LocalDate dataEmissao;
    private String assinaturaDigital;
}
