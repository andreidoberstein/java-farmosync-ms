package com.farmosync.inventory.infrastructure.messaging;

import com.farmosync.inventory.application.command.EstoqueAtualizadoDto;
import com.farmosync.inventory.application.ports.EstoqueAtualizadoEventPublisher;
import com.farmosync.inventory.infrastructure.messaging.event.EstoqueAtualizadoEvent;
import com.farmosync.inventory.infrastructure.messaging.event.ItemEstoqueAtualizadoEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class KafkaEstoqueAtualizadoEventPublisher implements EstoqueAtualizadoEventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "estoque-atualizado-topic";

    @Override
    public void publicarEstoqueAtualizado(EstoqueAtualizadoDto resultado) {
        EstoqueAtualizadoEvent event = EstoqueAtualizadoEvent.builder()
                .vendaId(resultado.getVendaId())
                .status(resultado.getStatus())
                .motivoRejeicao(resultado.getMotivoRejeicao())
                .itens(resultado.getItens() != null ? resultado.getItens().stream()
                        .map(item -> ItemEstoqueAtualizadoEvent.builder()
                                .produtoId(item.getProdutoId())
                                .numeroLote(item.getNumeroLote())
                                .quantidade(item.getQuantidade())
                                .build())
                        .collect(Collectors.toList()) : null)
                .build();

        kafkaTemplate.send(TOPIC, event.getVendaId(), event);
    }
}
