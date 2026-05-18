package com.farmosync.prescription.infrastructure.repository.mongo;

import com.farmosync.prescription.infrastructure.repository.document.AuditoriaReceitaDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface MongoAuditoriaReceitaRepository extends MongoRepository<AuditoriaReceitaDocument, String> {
    Optional<AuditoriaReceitaDocument> findByVendaId(String vendaId);
}
