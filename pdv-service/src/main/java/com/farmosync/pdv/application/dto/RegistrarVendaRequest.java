package com.farmosync.pdv.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
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
    private String cpfCliente;
    @NotEmpty
    @Valid
    private List<ItemRequest> itens;
}
