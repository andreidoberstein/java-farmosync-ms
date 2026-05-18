package com.farmosync.prescription.infrastructure.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceitaEmitidaEvent {
    private String crmMedico;
    private String crmUf;
    private String nomeMedico;
    private LocalDate dataEmissao;
    private String assinaturaDigital;
}
