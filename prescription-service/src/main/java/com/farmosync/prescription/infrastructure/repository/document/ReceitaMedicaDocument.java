package com.farmosync.prescription.infrastructure.repository.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceitaMedicaDocument {
    private String crmMedicoNumero;
    private String crmMedicoUf;
    private String nomeMedico;
    private LocalDate dataEmissao;
    private String assinaturaDigital;
}
