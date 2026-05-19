package com.farmosync.pdv.application.usecase;

import com.farmosync.pdv.application.command.AtualizarStatusVendaCommand;
import com.farmosync.pdv.domain.model.Venda;
import com.farmosync.pdv.domain.repository.VendaRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class AtualizarStatusVendaUseCase {

    private final VendaRepository vendaRepository;
    private final Counter baixasErrosCounter;

    public AtualizarStatusVendaUseCase(VendaRepository vendaRepository, MeterRegistry meterRegistry) {
        this.vendaRepository = vendaRepository;
        this.baixasErrosCounter = Counter.builder("farmosync.estoque.baixas.erros")
                .description("Total de falhas ou rejeicoes na baixa de estoque")
                .register(meterRegistry);
    }

    @Transactional
    public void executar(AtualizarStatusVendaCommand command) {
        log.info("Processando atualizacao de status da Venda ID: {} com resultado: {}.", 
                command.getVendaId(), command.getStatus());

        Venda venda = vendaRepository.buscarPorId(command.getVendaId())
                .orElseThrow(() -> new IllegalArgumentException("Venda nao encontrada: " + command.getVendaId()));

        if ("SUCESSO".equalsIgnoreCase(command.getStatus())) {
            venda.finalizar();
            log.info("Venda ID: {} finalizada com sucesso.", venda.getId());
        } else {
            venda.rejeitar();
            baixasErrosCounter.increment();
            log.warn("Venda ID: {} rejeitada. Motivo: {}", venda.getId(), command.getMotivoRejeicao());
        }

        vendaRepository.salvar(venda);
    }
}
