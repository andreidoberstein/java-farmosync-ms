package com.farmosync.inventory.infrastructure.repository;

import com.farmosync.inventory.domain.model.ProdutoEstoque;
import com.farmosync.inventory.domain.repository.ProdutoEstoqueRepository;
import com.farmosync.inventory.infrastructure.repository.document.ProdutoEstoqueDocument;
import com.farmosync.inventory.infrastructure.repository.mongo.MongoProdutoEstoqueRepository;
import com.mongodb.client.result.UpdateResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class ProdutoEstoqueRepositoryImpl implements ProdutoEstoqueRepository {

    private final MongoProdutoEstoqueRepository mongoRepository;
    private final MongoTemplate mongoTemplate;

    @Autowired
    public ProdutoEstoqueRepositoryImpl(MongoProdutoEstoqueRepository mongoRepository, MongoTemplate mongoTemplate) {
        this.mongoRepository = mongoRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Optional<ProdutoEstoque> buscarPorId(String id) {
        return mongoRepository.findById(id).map(ProdutoEstoqueDocument::toDomain);
    }

    @Override
    public ProdutoEstoque salvar(ProdutoEstoque produtoEstoque) {
        ProdutoEstoqueDocument doc = ProdutoEstoqueDocument.fromDomain(produtoEstoque);
        return mongoRepository.save(doc).toDomain();
    }

    @Override
    public boolean decrementarEstoqueAtomico(String produtoId, String numeroLote, int quantidade) {
        Query query = new Query(Criteria.where("_id").is(produtoId)
                .and("lotes").elemMatch(Criteria.where("numero").is(numeroLote)
                        .and("quantidade").gte(quantidade)));

        Update update = new Update().inc("lotes.$.quantidade", -quantidade);

        UpdateResult result = mongoTemplate.updateFirst(query, update, ProdutoEstoqueDocument.class);
        return result.getModifiedCount() > 0;
    }
}
