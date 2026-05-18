package com.farmosync.inventory.infrastructure.repository.mongo;

import com.farmosync.inventory.infrastructure.repository.document.ProdutoEstoqueDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MongoProdutoEstoqueRepository extends MongoRepository<ProdutoEstoqueDocument, String> {
}
