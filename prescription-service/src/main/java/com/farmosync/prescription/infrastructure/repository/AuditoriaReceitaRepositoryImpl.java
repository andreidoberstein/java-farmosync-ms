package com.farmosync.prescription.infrastructure.repository;

import com.farmosync.prescription.domain.model.AuditoriaReceita;
import com.farmosync.prescription.domain.repository.AuditoriaReceitaRepository;
import com.farmosync.prescription.infrastructure.repository.document.AuditoriaReceitaDocument;
import com.farmosync.prescription.infrastructure.repository.mongo.MongoAuditoriaReceitaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AuditoriaReceitaRepositoryImpl implements AuditoriaReceitaRepository {
    private final MongoAuditoriaReceitaRepository mongoRepository;

    @Override
    public AuditoriaReceita salvar(AuditoriaReceita auditoria) {
        AuditoriaReceitaDocument document = AuditoriaReceitaDocument.fromDomain(auditoria);
        AuditoriaReceitaDocument savedDocument = mongoRepository.save(document);
        return savedDocument.toDomain();
    }

    @Override
    public Optional<AuditoriaReceita> buscarPorId(String id) {
        return mongoRepository.findById(id).map(AuditoriaReceitaDocument::toDomain);
    }

    @Override
    public Optional<AuditoriaReceita> buscarPorVendaId(String vendaId) {
        return mongoRepository.findByVendaId(vendaId).map(AuditoriaReceitaDocument::toDomain);
    }
}
