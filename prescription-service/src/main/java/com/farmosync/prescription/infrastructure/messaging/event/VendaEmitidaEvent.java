package com.farmosync.prescription.infrastructure.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendaEmitidaEvent {
    private String vendaId;
    private String cpfCliente;
    private BigDecimal valorTotal;
    private LocalDateTime dataCriacao;
    private List<ItemVendaEmitidoEvent> itens;
    private ReceitaEmitidaEvent receita;
}
