package com.farmosync.prescription.infrastructure.messaging;

import com.farmosync.prescription.application.command.ItemVendaCommand;
import com.farmosync.prescription.application.command.ReceitaCommand;
import com.farmosync.prescription.application.command.ValidarReceitaCommand;
import com.farmosync.prescription.application.usecase.ValidarReceitaUseCase;
import com.farmosync.prescription.infrastructure.messaging.event.VendaEmitidaEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaVendaEmitidaConsumer {
    private final ValidarReceitaUseCase useCase;

    @KafkaListener(topics = "${app.kafka.topics.venda-emitida}", groupId = "prescription-group")
    public void consumir(VendaEmitidaEvent event) {
        log.info("Consumido evento de venda emitida do Kafka para a Venda ID: {}.", event.getVendaId());
        try {
            ValidarReceitaCommand command = ValidarReceitaCommand.builder()
                    .vendaId(event.getVendaId())
                    .cpfCliente(event.getCpfCliente())
                    .dataCriacao(event.getDataCriacao())
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
                    .receita(event.getReceita() != null ? ReceitaCommand.builder()
                            .crmMedico(event.getReceita().getCrmMedico())
                            .crmUf(event.getReceita().getCrmUf())
                            .nomeMedico(event.getReceita().getNomeMedico())
                            .dataEmissao(event.getReceita().getDataEmissao())
                            .assinaturaDigital(event.getReceita().getAssinaturaDigital())
                            .build() : null)
                    .build();

            useCase.processarValidacao(command);
            log.info("Evento de venda emitida processado com sucesso para a Venda ID: {}.", event.getVendaId());
        } catch (Exception e) {
            log.error("Erro ao processar validacao de receita para a Venda ID: {}. Causa: {}", event.getVendaId(), e.getMessage(), e);
            throw e;
        }
    }
}
