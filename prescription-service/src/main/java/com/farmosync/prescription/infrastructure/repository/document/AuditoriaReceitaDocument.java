package com.farmosync.prescription.infrastructure.repository.document;

import com.farmosync.prescription.domain.model.AuditoriaReceita;
import com.farmosync.prescription.domain.model.CRM;
import com.farmosync.prescription.domain.model.ReceitaMedica;
import com.farmosync.prescription.domain.model.StatusValidacao;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "auditoria_receitas")
public class AuditoriaReceitaDocument {
    @Id
    private String id;
    private String vendaId;
    private String cpfCliente;
    private ReceitaMedicaDocument receita;
    private String status;
    private String motivoRejeicao;
    private LocalDateTime dataValidacao;

    public static AuditoriaReceitaDocument fromDomain(AuditoriaReceita domain) {
        if (domain == null) return null;
        return AuditoriaReceitaDocument.builder()
                .id(domain.getId())
                .vendaId(domain.getVendaId())
                .cpfCliente(domain.getCpfCliente())
                .status(domain.getStatus() != null ? domain.getStatus().name() : null)
                .motivoRejeicao(domain.getMotivoRejeicao())
                .dataValidacao(domain.getDataValidacao())
                .receita(domain.getReceita() != null ? ReceitaMedicaDocument.builder()
                        .crmMedicoNumero(domain.getReceita().getCrmMedico().getNumero())
                        .crmMedicoUf(domain.getReceita().getCrmMedico().getUf())
                        .nomeMedico(domain.getReceita().getNomeMedico())
                        .dataEmissao(domain.getReceita().getDataEmissao())
                        .assinaturaDigital(domain.getReceita().getAssinaturaDigital())
                        .build() : null)
                .build();
    }

    public AuditoriaReceita toDomain() {
        return AuditoriaReceita.builder()
                .id(this.id)
                .vendaId(this.vendaId)
                .cpfCliente(this.cpfCliente)
                .status(this.status != null ? StatusValidacao.valueOf(this.status) : null)
                .motivoRejeicao(this.motivoRejeicao)
                .dataValidacao(this.dataValidacao)
                .receita(this.receita != null ? ReceitaMedica.builder()
                        .crmMedico(new CRM(this.receita.getCrmMedicoNumero(), this.receita.getCrmMedicoUf()))
                        .nomeMedico(this.receita.getNomeMedico())
                        .dataEmissao(this.receita.getDataEmissao())
                        .assinaturaDigital(this.receita.getAssinaturaDigital())
                        .build() : null)
                .build();
    }
}
