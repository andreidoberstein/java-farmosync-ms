package com.farmosync.pdv.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequest {
    @NotBlank
    private String produtoId;
    @NotBlank
    private String nomeProduto;
    @NotNull
    @Positive
    private Integer quantidade;
    @NotNull
    @Positive
    private BigDecimal precoUnitario;
    private String numeroLote;
    private LocalDate dataValidade;
    private boolean controlado;
}
