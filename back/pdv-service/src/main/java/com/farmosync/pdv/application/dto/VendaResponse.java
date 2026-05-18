package com.farmosync.pdv.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendaResponse {
    private String id;
    private String cpfCliente;
    private BigDecimal valorTotal;
    private String status;
    private LocalDateTime dataCriacao;
}
