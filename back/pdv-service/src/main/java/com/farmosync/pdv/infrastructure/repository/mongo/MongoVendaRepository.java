package com.farmosync.pdv.infrastructure.repository.mongo;

import com.farmosync.pdv.infrastructure.repository.document.VendaDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MongoVendaRepository extends MongoRepository<VendaDocument, String> {
}
