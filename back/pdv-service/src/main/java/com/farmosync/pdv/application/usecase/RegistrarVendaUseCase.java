package com.farmosync.pdv.application.usecase;

import com.farmosync.pdv.application.dto.ItemRequest;
import com.farmosync.pdv.application.dto.RegistrarVendaRequest;
import com.farmosync.pdv.application.dto.VendaResponse;
import com.farmosync.pdv.application.ports.VendaEventPublisher;
import com.farmosync.pdv.domain.model.ItemVenda;
import com.farmosync.pdv.domain.model.Lote;
import com.farmosync.pdv.domain.model.Receita;
import com.farmosync.pdv.domain.model.Venda;
import com.farmosync.pdv.domain.model.VendaStatus;
import com.farmosync.pdv.domain.repository.VendaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegistrarVendaUseCase {
    private final VendaRepository vendaRepository;
    private final VendaEventPublisher vendaEventPublisher;

    @Transactional
    public VendaResponse executar(RegistrarVendaRequest request) {
        List<ItemVenda> domainItens = request.getItens().stream()
                .map(this::mapToDomainItem)
                .collect(Collectors.toList());

        Receita domainReceita = null;
        if (request.getReceita() != null) {
            domainReceita = Receita.builder()
                    .crmMedico(request.getReceita().getCrmMedico())
                    .crmUf(request.getReceita().getCrmUf())
                    .nomeMedico(request.getReceita().getNomeMedico())
                    .dataEmissao(request.getReceita().getDataEmissao())
                    .assinaturaDigital(request.getReceita().getAssinaturaDigital())
                    .build();
        }

        Venda venda = Venda.builder()
                .id(UUID.randomUUID().toString())
                .cpfCliente(request.getCpfCliente())
                .itens(domainItens)
                .status(VendaStatus.PENDENTE)
                .dataCriacao(LocalDateTime.now())
                .receita(domainReceita)
                .build();

        venda.calcularValorTotal();

        String eventPayload = vendaEventPublisher.obterPayloadVendaEmitida(venda);
        Venda savedVenda = vendaRepository.salvarComEvento(venda, eventPayload);

        return VendaResponse.builder()
                .id(savedVenda.getId())
                .cpfCliente(savedVenda.getCpfCliente())
                .valorTotal(savedVenda.getValorTotal())
                .status(savedVenda.getStatus().name())
                .dataCriacao(savedVenda.getDataCriacao())
                .build();
    }

    private ItemVenda mapToDomainItem(ItemRequest itemRequest) {
        Lote lote = null;
        if (itemRequest.getNumeroLote() != null && !itemRequest.getNumeroLote().isBlank()) {
            lote = Lote.builder()
                    .numeroLote(itemRequest.getNumeroLote())
                    .dataValidade(itemRequest.getDataValidade())
                    .build();
        }

        return ItemVenda.builder()
                .produtoId(itemRequest.getProdutoId())
                .nomeProduto(itemRequest.getNomeProduto())
                .quantidade(itemRequest.getQuantidade())
                .precoUnitario(itemRequest.getPrecoUnitario())
                .lote(lote)
                .controlado(itemRequest.isControlado())
                .build();
    }
}
