package com.farmosync.pdv.infrastructure.repository.document;

import com.farmosync.pdv.domain.model.Lote;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoteDocument {
    private String numeroLote;
    private LocalDate dataValidade;

    public static LoteDocument fromDomain(Lote domain) {
        if (domain == null) return null;
        return LoteDocument.builder()
                .numeroLote(domain.getNumeroLote())
                .dataValidade(domain.getDataValidade())
                .build();
    }

    public Lote toDomain() {
        return Lote.builder()
                .numeroLote(this.numeroLote)
                .dataValidade(this.dataValidade)
                .build();
    }
}
