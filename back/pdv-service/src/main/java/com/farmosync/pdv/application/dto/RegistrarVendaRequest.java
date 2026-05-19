package com.farmosync.pdv.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrarVendaRequest {
    @NotBlank
    @Pattern(regexp = "\\d{11}", message = "O CPF deve conter exatamente 11 digitos numericos")
    private String cpfCliente;
    @NotEmpty
    @Valid
    private List<ItemRequest> itens;
    @Valid
    private ReceitaRequest receita;
}
