package com.farmosync.inventory.application.usecase;

import com.farmosync.inventory.application.command.BaixarEstoqueCommand;
import com.farmosync.inventory.application.command.EstoqueAtualizadoDto;
import com.farmosync.inventory.application.command.ItemEstoqueAtualizadoDto;
import com.farmosync.inventory.application.command.ItemVendaCommand;
import com.farmosync.inventory.application.ports.EstoqueAtualizadoEventPublisher;
import com.farmosync.inventory.domain.model.ProdutoEstoque;
import com.farmosync.inventory.domain.repository.ProdutoEstoqueRepository;
import com.farmosync.inventory.infrastructure.repository.document.IdempotenceKeyDocument;
import com.farmosync.inventory.infrastructure.repository.mongo.MongoIdempotenceKeyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BaixarEstoqueUseCase {

    private final ProdutoEstoqueRepository repository;
    private final EstoqueAtualizadoEventPublisher publisher;
    private final MongoIdempotenceKeyRepository idempotenceRepository;

    public void processarBaixaEstoque(BaixarEstoqueCommand command) {
        try {
            idempotenceRepository.insert(IdempotenceKeyDocument.builder()
                    .id(command.getVendaId())
                    .dataProcessamento(LocalDateTime.now())
                    .build());
        } catch (DuplicateKeyException e) {
            return;
        }

        List<ItemEstoqueAtualizadoDto> mappedItens = command.getItens() != null ? command.getItens().stream()
                .map(i -> ItemEstoqueAtualizadoDto.builder()
                        .produtoId(i.getProdutoId())
                        .numeroLote(i.getNumeroLote())
                        .quantidade(i.getQuantidade())
                        .build())
                .collect(Collectors.toList()) : new ArrayList<>();

        if (!"APROVADA".equalsIgnoreCase(command.getStatus())) {
            EstoqueAtualizadoDto erroEvent = EstoqueAtualizadoDto.builder()
                    .vendaId(command.getVendaId())
                    .status("ERRO")
                    .motivoRejeicao("Receita medica rejeitada no PDV: " + command.getMotivoRejeicao())
                    .itens(mappedItens)
                    .build();
            publisher.publicarEstoqueAtualizado(erroEvent);
            return;
        }

        List<DeducaoLocal> deducoesEfetuadas = new ArrayList<>();

        try {
            for (ItemVendaCommand item : command.getItens()) {
                ProdutoEstoque produto = repository.buscarPorId(item.getProdutoId())
                        .orElseThrow(() -> new IllegalArgumentException("Produto " + item.getProdutoId() + " nao cadastrado."));

                produto.baixarEstoque(item.getNumeroLote(), item.getQuantidade(), LocalDate.now());

                boolean sucesso = repository.decrementarEstoqueAtomico(
                        item.getProdutoId(),
                        item.getNumeroLote(),
                        item.getQuantidade()
                );

                if (!sucesso) {
                    throw new IllegalStateException("Saldo insuficiente concorrente no lote " + item.getNumeroLote() + 
                            " para o produto " + item.getProdutoId() + ". A transacao concorrente alterou o saldo.");
                }

                deducoesEfetuadas.add(new DeducaoLocal(item.getProdutoId(), item.getNumeroLote(), item.getQuantidade()));
            }

            EstoqueAtualizadoDto sucessoEvent = EstoqueAtualizadoDto.builder()
                    .vendaId(command.getVendaId())
                    .status("SUCESSO")
                    .itens(mappedItens)
                    .build();
            publisher.publicarEstoqueAtualizado(sucessoEvent);

        } catch (Exception e) {
            for (DeducaoLocal d : deducoesEfetuadas) {
                repository.decrementarEstoqueAtomico(d.produtoId, d.numeroLote, -d.quantidade);
            }

            EstoqueAtualizadoDto erroEvent = EstoqueAtualizadoDto.builder()
                    .vendaId(command.getVendaId())
                    .status("ERRO")
                    .motivoRejeicao(e.getMessage())
                    .itens(mappedItens)
                    .build();
            publisher.publicarEstoqueAtualizado(erroEvent);
        }
    }

    private static class DeducaoLocal {
        private final String produtoId;
        private final String numeroLote;
        private final int quantidade;

        public DeducaoLocal(String produtoId, String numeroLote, int quantidade) {
            this.produtoId = produtoId;
            this.numeroLote = numeroLote;
            this.quantidade = quantidade;
        }
    }
}
