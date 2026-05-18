package com.farmosync.inventory.infrastructure.repository.document;

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
    private String numero;
    private int quantidade;
    private LocalDate dataValidade;
}
