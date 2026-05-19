package com.farmosync.pdv.infrastructure.repository.mongo;

import com.farmosync.pdv.infrastructure.repository.document.OutboxEventDocument;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MongoOutboxEventRepository extends MongoRepository<OutboxEventDocument, String> {
    List<OutboxEventDocument> findByStatus(String status, Pageable pageable);
}
