package com.farmosync.pdv.application.command;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AtualizarStatusVendaCommand {
    private String vendaId;
    private String status;
    private String motivoRejeicao;
}
