package com.farmosync.inventory.infrastructure.messaging;

import com.farmosync.inventory.application.command.BaixarEstoqueCommand;
import com.farmosync.inventory.application.command.ItemVendaCommand;
import com.farmosync.inventory.application.usecase.BaixarEstoqueUseCase;
import com.farmosync.inventory.infrastructure.messaging.event.ReceitaValidadaEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReceitaValidadaConsumer {
    private final BaixarEstoqueUseCase useCase;

    @KafkaListener(topics = "${app.kafka.topics.receita-validada}", groupId = "inventory-group")
    public void consumir(ReceitaValidadaEvent event) {
        log.info("Consumido evento de receita validada do Kafka para a Venda ID: {}. Status da receita: {}.", 
                event.getVendaId(), event.getStatus());
        try {
            BaixarEstoqueCommand command = BaixarEstoqueCommand.builder()
                    .vendaId(event.getVendaId())
                    .cpfCliente(event.getCpfCliente())
                    .status(event.getStatus())
                    .motivoRejeicao(event.getMotivoRejeicao())
                    .itens(event.getItens() != null ? event.getItens().stream()
                            .map(item -> ItemVendaCommand.builder()
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

            useCase.processarBaixaEstoque(command);
            log.info("Evento de receita validada processado com sucesso para a Venda ID: {}.", event.getVendaId());
        } catch (Exception e) {
            log.error("Erro ao processar baixa de estoque para a Venda ID: {}. Causa: {}", event.getVendaId(), e.getMessage(), e);
            throw e;
        }
    }
}
