package com.farmosync.inventory.infrastructure.repository.document;

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
@Document(collection = "chaves_idempotencia")
public class IdempotenceKeyDocument {
    @Id
    private String id;
    private LocalDateTime dataProcessamento;
}
