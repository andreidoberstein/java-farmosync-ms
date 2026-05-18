package com.farmosync.pdv.infrastructure.repository;

import com.farmosync.pdv.domain.model.Venda;
import com.farmosync.pdv.domain.repository.VendaRepository;
import com.farmosync.pdv.infrastructure.repository.document.VendaDocument;
import com.farmosync.pdv.infrastructure.repository.mongo.MongoVendaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class VendaRepositoryImpl implements VendaRepository {
    private final MongoVendaRepository mongoVendaRepository;

    @Override
    public Venda salvar(Venda venda) {
        VendaDocument document = VendaDocument.fromDomain(venda);
        VendaDocument savedDocument = mongoVendaRepository.save(document);
        return savedDocument.toDomain();
    }

    @Override
    public Optional<Venda> buscarPorId(String id) {
        return mongoVendaRepository.findById(id)
                .map(VendaDocument::toDomain);
    }
}
