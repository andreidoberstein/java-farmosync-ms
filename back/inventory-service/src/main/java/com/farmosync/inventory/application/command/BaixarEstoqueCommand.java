package com.farmosync.inventory.application.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaixarEstoqueCommand {
    private String vendaId;
    private String cpfCliente;
    private String status;
    private String motivoRejeicao;
    private List<ItemVendaCommand> itens;
}
