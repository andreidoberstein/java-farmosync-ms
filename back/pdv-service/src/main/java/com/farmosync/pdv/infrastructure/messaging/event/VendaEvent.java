package com.farmosync.pdv.infrastructure.messaging.event;

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
public class VendaEvent {
    private String vendaId;
    private String cpfCliente;
    private BigDecimal valorTotal;
    private LocalDateTime dataCriacao;
    private List<ItemEvent> itens;
    private ReceitaEvent receita;
}
