package com.farmosync.prescription.application.usecase;

import com.farmosync.prescription.application.command.ItemVendaCommand;
import com.farmosync.prescription.application.command.ReceitaValidadaDto;
import com.farmosync.prescription.application.command.ValidarReceitaCommand;
import com.farmosync.prescription.application.ports.ReceitaValidadaEventPublisher;
import com.farmosync.prescription.domain.model.AuditoriaReceita;
import com.farmosync.prescription.domain.model.CRM;
import com.farmosync.prescription.domain.model.ReceitaMedica;
import com.farmosync.prescription.domain.repository.AuditoriaReceitaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ValidarReceitaUseCase {
    private final AuditoriaReceitaRepository auditoriaRepository;
    private final ReceitaValidadaEventPublisher eventPublisher;

    public void processarValidacao(ValidarReceitaCommand command) {
        boolean contemControlados = command.getItens().stream()
                .anyMatch(ItemVendaCommand::isControlado);

        AuditoriaReceita auditoria = AuditoriaReceita.builder()
                .id(UUID.randomUUID().toString())
                .vendaId(command.getVendaId())
                .cpfCliente(command.getCpfCliente())
                .dataValidacao(LocalDateTime.now())
                .build();

        if (!contemControlados) {
            auditoria.aprovar();
        } else if (command.getReceita() == null) {
            auditoria.rejeitar("Venda contem medicamentos controlados, mas nenhuma receita medica foi apresentada.");
        } else {
            try {
                CRM domainCrm = new CRM(
                        command.getReceita().getCrmMedico(),
                        command.getReceita().getCrmUf()
                );

                ReceitaMedica domainReceita = ReceitaMedica.builder()
                        .crmMedico(domainCrm)
                        .nomeMedico(command.getReceita().getNomeMedico())
                        .dataEmissao(command.getReceita().getDataEmissao())
                        .assinaturaDigital(command.getReceita().getAssinaturaDigital())
                        .build();

                auditoria.setReceita(domainReceita);

                if (domainReceita.isExpirada(command.getDataCriacao().toLocalDate())) {
                    auditoria.rejeitar("A receita medica apresentada esta expirada (validade de 10 dias).");
                } else {
                    auditoria.aprovar();
                }
            } catch (IllegalArgumentException e) {
                auditoria.rejeitar("Dados da receita invalidos: " + e.getMessage());
            }
        }

        auditoriaRepository.salvar(auditoria);

        ReceitaValidadaDto outcomeEvent = ReceitaValidadaDto.builder()
                .vendaId(auditoria.getVendaId())
                .cpfCliente(auditoria.getCpfCliente())
                .status(auditoria.getStatus().name())
                .motivoRejeicao(auditoria.getMotivoRejeicao())
                .itens(command.getItens())
                .build();

        eventPublisher.publicarReceitaValidada(outcomeEvent);
    }
}
