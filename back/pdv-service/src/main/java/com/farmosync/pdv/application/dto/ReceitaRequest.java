package com.farmosync.pdv.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceitaRequest {
    @NotBlank
    private String crmMedico;
    @NotBlank
    @Size(min = 2, max = 2)
    private String crmUf;
    @NotBlank
    private String nomeMedico;
    @NotNull
    private LocalDate dataEmissao;
    @NotBlank
    private String assinaturaDigital;
}
