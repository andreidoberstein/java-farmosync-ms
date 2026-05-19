package com.farmosync.prescription.application.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidarReceitaCommand {
    private String vendaId;
    private String cpfCliente;
    private LocalDateTime dataCriacao;
    private List<ItemVendaCommand> itens;
    private ReceitaCommand receita;
}
