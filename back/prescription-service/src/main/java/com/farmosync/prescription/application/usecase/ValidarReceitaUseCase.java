package com.farmosync.prescription.application.usecase;

import com.farmosync.prescription.application.ports.ReceitaValidadaEventPublisher;
import com.farmosync.prescription.domain.model.AuditoriaReceita;
import com.farmosync.prescription.domain.model.CRM;
import com.farmosync.prescription.domain.model.ReceitaMedica;
import com.farmosync.prescription.domain.model.StatusValidacao;
import com.farmosync.prescription.domain.repository.AuditoriaReceitaRepository;
import com.farmosync.prescription.infrastructure.messaging.event.ItemVendaEmitidoEvent;
import com.farmosync.prescription.infrastructure.messaging.event.ReceitaValidadaEvent;
import com.farmosync.prescription.infrastructure.messaging.event.VendaEmitidaEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ValidarReceitaUseCase {
    private final AuditoriaReceitaRepository auditoriaRepository;
    private final ReceitaValidadaEventPublisher eventPublisher;

    public void processarValidacao(VendaEmitidaEvent event) {
        boolean contemControlados = event.getItens().stream()
                .anyMatch(ItemVendaEmitidoEvent::isControlado);

        AuditoriaReceita auditoria = AuditoriaReceita.builder()
                .id(UUID.randomUUID().toString())
                .vendaId(event.getVendaId())
                .cpfCliente(event.getCpfCliente())
                .dataValidacao(LocalDateTime.now())
                .build();

        if (!contemControlados) {
            auditoria.aprovar();
        } else if (event.getReceita() == null) {
            auditoria.rejeitar("Venda contem medicamentos controlados, mas nenhuma receita medica foi apresentada.");
        } else {
            try {
                CRM domainCrm = new CRM(
                        event.getReceita().getCrmMedico(),
                        event.getReceita().getCrmUf()
                );

                ReceitaMedica domainReceita = ReceitaMedica.builder()
                        .crmMedico(domainCrm)
                        .nomeMedico(event.getReceita().getNomeMedico())
                        .dataEmissao(event.getReceita().getDataEmissao())
                        .assinaturaDigital(event.getReceita().getAssinaturaDigital())
                        .build();

                auditoria.setReceita(domainReceita);

                if (domainReceita.isExpirada(event.getDataCriacao().toLocalDate())) {
                    auditoria.rejeitar("A receita medica apresentada esta expirada (validade de 10 dias).");
                } else {
                    auditoria.aprovar();
                }
            } catch (IllegalArgumentException e) {
                auditoria.rejeitar("Dados da receita invalidos: " + e.getMessage());
            }
        }

        auditoriaRepository.salvar(auditoria);

        ReceitaValidadaEvent outcomeEvent = ReceitaValidadaEvent.builder()
                .vendaId(auditoria.getVendaId())
                .cpfCliente(auditoria.getCpfCliente())
                .status(auditoria.getStatus().name())
                .motivoRejeicao(auditoria.getMotivoRejeicao())
                .itens(event.getItens())
                .build();

        eventPublisher.publicarReceitaValidada(outcomeEvent);
    }
}
