package com.farmosync.prescription.infrastructure.messaging;

import com.farmosync.prescription.application.command.ReceitaValidadaDto;
import com.farmosync.prescription.application.ports.ReceitaValidadaEventPublisher;
import com.farmosync.prescription.infrastructure.messaging.event.ItemVendaEmitidoEvent;
import com.farmosync.prescription.infrastructure.messaging.event.ReceitaValidadaEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class KafkaReceitaValidadaEventPublisher implements ReceitaValidadaEventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "receita-validada-topic";

    @Override
    public void publicarReceitaValidada(ReceitaValidadaDto resultado) {
        ReceitaValidadaEvent event = ReceitaValidadaEvent.builder()
                .vendaId(resultado.getVendaId())
                .cpfCliente(resultado.getCpfCliente())
                .status(resultado.getStatus())
                .motivoRejeicao(resultado.getMotivoRejeicao())
                .itens(resultado.getItens() != null ? resultado.getItens().stream()
                        .map(item -> ItemVendaEmitidoEvent.builder()
                                .produtoId(item.getProdutoId())
                                .nomeProduto(item.getNomeProduto())
                                .quantidade(item.getQuantidade())
                                .precoUnitario(item.getPrecoUnitario())
                                .numeroLote(item.getNumeroLote())
                                .dataValidade(item.getDataValidade())
                                .controlado(item.isControlado())
                                .build())
                        .collect(Collectors.toList()) : null)
                .build();

        kafkaTemplate.send(TOPIC, event.getVendaId(), event);
    }
}
