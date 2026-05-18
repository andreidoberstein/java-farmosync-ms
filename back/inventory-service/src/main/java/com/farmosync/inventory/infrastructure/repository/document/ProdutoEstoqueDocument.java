package com.farmosync.inventory.infrastructure.repository.document;

import com.farmosync.inventory.domain.model.Lote;
import com.farmosync.inventory.domain.model.ProdutoEstoque;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "produtos_estoque")
public class ProdutoEstoqueDocument {
    @Id
    private String id;
    private String nome;
    @Builder.Default
    private List<LoteDocument> lotes = new ArrayList<>();

    public static ProdutoEstoqueDocument fromDomain(ProdutoEstoque domain) {
        if (domain == null) return null;
        return ProdutoEstoqueDocument.builder()
                .id(domain.getId())
                .nome(domain.getNome())
                .lotes(domain.getLotes() != null ? domain.getLotes().stream()
                        .map(l -> LoteDocument.builder()
                                .numero(l.getNumero())
                                .quantidade(l.getQuantidade())
                                .dataValidade(l.getDataValidade())
                                .build())
                        .collect(Collectors.toList()) : new ArrayList<>())
                .build();
    }

    public ProdutoEstoque toDomain() {
        return ProdutoEstoque.builder()
                .id(this.id)
                .nome(this.nome)
                .lotes(this.lotes != null ? this.lotes.stream()
                        .map(l -> Lote.builder()
                                .numero(l.getNumero())
                                .quantidade(l.getQuantidade())
                                .dataValidade(l.getDataValidade())
                                .build())
                        .collect(Collectors.toList()) : new ArrayList<>())
                .build();
    }
}
