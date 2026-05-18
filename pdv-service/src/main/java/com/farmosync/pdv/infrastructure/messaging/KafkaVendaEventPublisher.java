package com.farmosync.pdv.infrastructure.messaging;

import com.farmosync.pdv.application.ports.VendaEventPublisher;
import com.farmosync.pdv.domain.model.Venda;
import com.farmosync.pdv.infrastructure.messaging.event.ItemEvent;
import com.farmosync.pdv.infrastructure.messaging.event.VendaEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class KafkaVendaEventPublisher implements VendaEventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "venda-emitida-topic";

    @Override
    public void publicarVendaEmitida(Venda venda) {
        List<ItemEvent> itemEvents = venda.getItens().stream()
                .map(item -> ItemEvent.builder()
                        .produtoId(item.getProdutoId())
                        .nomeProduto(item.getNomeProduto())
                        .quantidade(item.getQuantidade())
                        .precoUnitario(item.getPrecoUnitario())
                        .numeroLote(item.getLote() != null ? item.getLote().getNumeroLote() : null)
                        .dataValidade(item.getLote() != null ? item.getLote().getDataValidade() : null)
                        .controlado(item.isControlado())
                        .build())
                .collect(Collectors.toList());

        VendaEvent event = VendaEvent.builder()
                .vendaId(venda.getId())
                .cpfCliente(venda.getCpfCliente())
                .valorTotal(venda.getValorTotal())
                .dataCriacao(venda.getDataCriacao())
                .itens(itemEvents)
                .build();

        kafkaTemplate.send(TOPIC, venda.getId(), event);
    }
}
