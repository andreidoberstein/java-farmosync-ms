package com.farmosync.pdv.infrastructure.messaging;

import com.farmosync.pdv.application.command.AtualizarStatusVendaCommand;
import com.farmosync.pdv.application.usecase.AtualizarStatusVendaUseCase;
import com.farmosync.pdv.infrastructure.messaging.event.EstoqueAtualizadoEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaEstoqueAtualizadoConsumer {

    private final AtualizarStatusVendaUseCase useCase;

    @KafkaListener(topics = "${app.kafka.topics.estoque-atualizado}", groupId = "pdv-group")
    public void consumir(EstoqueAtualizadoEvent event) {
        log.info("Consumido evento de estoque atualizado do Kafka para a Venda ID: {}. Status: {}.", 
                event.getVendaId(), event.getStatus());
        try {
            AtualizarStatusVendaCommand command = AtualizarStatusVendaCommand.builder()
                    .vendaId(event.getVendaId())
                    .status(event.getStatus())
                    .motivoRejeicao(event.getMotivoRejeicao())
                    .build();

            useCase.executar(command);
            log.info("Status da venda ID: {} atualizado com sucesso.", event.getVendaId());
        } catch (Exception e) {
            log.error("Erro ao processar atualizacao de status da Venda ID: {}. Causa: {}", event.getVendaId(), e.getMessage(), e);
            throw e;
        }
    }
}
