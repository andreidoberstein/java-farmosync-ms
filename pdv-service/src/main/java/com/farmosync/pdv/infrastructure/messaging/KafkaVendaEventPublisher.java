package com.farmosync.pdv.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.farmosync.pdv.application.ports.VendaEventPublisher;
import com.farmosync.pdv.domain.model.Venda;
import com.farmosync.pdv.infrastructure.messaging.event.ItemEvent;
import com.farmosync.pdv.infrastructure.messaging.event.ReceitaEvent;
import com.farmosync.pdv.infrastructure.messaging.event.VendaEvent;
import com.farmosync.pdv.infrastructure.repository.document.OutboxEventDocument;
import com.farmosync.pdv.infrastructure.repository.mongo.MongoOutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaVendaEventPublisher implements VendaEventPublisher {
    private final MongoOutboxEventRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void publicarVendaEmitida(Venda venda) {
        log.info("Recebida solicitacao para publicar evento de venda emitida. ID Venda: {}. Total: {}.", 
                venda.getId(), venda.getValorTotal());

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

        ReceitaEvent receiverEvent = null;
        if (venda.getReceita() != null) {
            receiverEvent = ReceitaEvent.builder()
                    .crmMedico(venda.getReceita().getCrmMedico())
                    .crmUf(venda.getReceita().getCrmUf())
                    .nomeMedico(venda.getReceita().getNomeMedico())
                    .dataEmissao(venda.getReceita().getDataEmissao())
                    .assinaturaDigital(venda.getReceita().getAssinaturaDigital())
                    .build();
        }

        VendaEvent event = VendaEvent.builder()
                .vendaId(venda.getId())
                .cpfCliente(venda.getCpfCliente())
                .valorTotal(venda.getValorTotal())
                .dataCriacao(venda.getDataCriacao())
                .itens(itemEvents)
                .receita(receiverEvent)
                .build();

        try {
            String payload = objectMapper.writeValueAsString(event);
            OutboxEventDocument outboxEvent = OutboxEventDocument.builder()
                    .id(UUID.randomUUID().toString())
                    .aggregateType("Venda")
                    .aggregateId(venda.getId())
                    .eventType("VendaEmitidaEvent")
                    .payload(payload)
                    .status("PENDING")
                    .dataCriacao(LocalDateTime.now())
                    .build();
            outboxRepository.save(outboxEvent);
            log.info("Intencao de evento do outbox ID: {} registrada com sucesso para a Venda ID: {}.", 
                    outboxEvent.getId(), venda.getId());
        } catch (Exception e) {
            log.error("Erro critico ao serializar ou persistir evento no Outbox para a Venda ID: {}.", venda.getId(), e);
            throw new RuntimeException("Falha ao salvar evento no Outbox", e);
        }
    }
}
