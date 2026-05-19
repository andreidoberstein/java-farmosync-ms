package com.farmosync.pdv.infrastructure.repository;

import com.farmosync.pdv.domain.model.Venda;
import com.farmosync.pdv.domain.repository.VendaRepository;
import com.farmosync.pdv.infrastructure.repository.document.VendaDocument;
import com.farmosync.pdv.infrastructure.repository.mongo.MongoVendaRepository;
import com.farmosync.pdv.infrastructure.repository.document.OutboxEventDocument;
import com.farmosync.pdv.infrastructure.repository.mongo.MongoOutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class VendaRepositoryImpl implements VendaRepository {
    private final MongoVendaRepository mongoVendaRepository;
    private final MongoOutboxEventRepository mongoOutboxEventRepository;

    @Override
    public Venda salvar(Venda venda) {
        VendaDocument document = VendaDocument.fromDomain(venda);
        VendaDocument savedDocument = mongoVendaRepository.save(document);
        return savedDocument.toDomain();
    }

    @Override
    @Transactional
    public Venda salvarComEvento(Venda venda, String eventPayload) {
        VendaDocument document = VendaDocument.fromDomain(venda);
        VendaDocument savedDocument = mongoVendaRepository.save(document);

        OutboxEventDocument outboxEvent = OutboxEventDocument.builder()
                .id(UUID.randomUUID().toString())
                .aggregateType("Venda")
                .aggregateId(venda.getId())
                .eventType("VendaEmitidaEvent")
                .payload(eventPayload)
                .status("PENDING")
                .dataCriacao(LocalDateTime.now())
                .build();
        mongoOutboxEventRepository.save(outboxEvent);

        return savedDocument.toDomain();
    }

    @Override
    public Optional<Venda> buscarPorId(String id) {
        return mongoVendaRepository.findById(id)
                .map(VendaDocument::toDomain);
    }
}
