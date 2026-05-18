package com.farmosync.pdv.infrastructure.repository.document;

import com.farmosync.pdv.domain.model.Receita;
import com.farmosync.pdv.domain.model.Venda;
import com.farmosync.pdv.domain.model.VendaStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "vendas")
public class VendaDocument {
    @Id
    private String id;
    private List<ItemVendaDocument> itens;
    private String cpfCliente;
    private BigDecimal valorTotal;
    private String status;
    private LocalDateTime dataCriacao;
    private ReceitaDocument receita;

    public static VendaDocument fromDomain(Venda domain) {
        if (domain == null) return null;
        return VendaDocument.builder()
                .id(domain.getId())
                .cpfCliente(domain.getCpfCliente())
                .itens(domain.getItens() != null ? domain.getItens().stream()
                        .map(ItemVendaDocument::fromDomain)
                        .collect(Collectors.toList()) : null)
                .valorTotal(domain.getValorTotal())
                .status(domain.getStatus() != null ? domain.getStatus().name() : null)
                .dataCriacao(domain.getDataCriacao())
                .receita(domain.getReceita() != null ? ReceitaDocument.builder()
                        .crmMedico(domain.getReceita().getCrmMedico())
                        .crmUf(domain.getReceita().getCrmUf())
                        .nomeMedico(domain.getReceita().getNomeMedico())
                        .dataEmissao(domain.getReceita().getDataEmissao())
                        .assinaturaDigital(domain.getReceita().getAssinaturaDigital())
                        .build() : null)
                .build();
    }

    public Venda toDomain() {
        return Venda.builder()
                .id(this.id)
                .cpfCliente(this.cpfCliente)
                .itens(this.itens != null ? this.itens.stream()
                        .map(ItemVendaDocument::toDomain)
                        .collect(Collectors.toList()) : null)
                .valorTotal(this.valorTotal)
                .status(this.status != null ? VendaStatus.valueOf(this.status) : null)
                .dataCriacao(this.dataCriacao)
                .receita(this.receita != null ? Receita.builder()
                        .crmMedico(this.receita.getCrmMedico())
                        .crmUf(this.receita.getCrmUf())
                        .nomeMedico(this.receita.getNomeMedico())
                        .dataEmissao(this.receita.getDataEmissao())
                        .assinaturaDigital(this.receita.getAssinaturaDigital())
                        .build() : null)
                .build();
    }
}
