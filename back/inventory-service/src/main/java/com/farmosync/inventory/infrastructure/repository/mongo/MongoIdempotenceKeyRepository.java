package com.farmosync.inventory.infrastructure.repository.mongo;

import com.farmosync.inventory.infrastructure.repository.document.IdempotenceKeyDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MongoIdempotenceKeyRepository extends MongoRepository<IdempotenceKeyDocument, String> {
}
