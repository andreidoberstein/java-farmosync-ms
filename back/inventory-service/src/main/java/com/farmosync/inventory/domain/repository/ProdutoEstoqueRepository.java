package com.farmosync.inventory.domain.repository;

import com.farmosync.inventory.domain.model.ProdutoEstoque;
import java.util.Optional;

public interface ProdutoEstoqueRepository {
    Optional<ProdutoEstoque> buscarPorId(String id);
    ProdutoEstoque salvar(ProdutoEstoque produtoEstoque);
    boolean decrementarEstoqueAtomico(String produtoId, String numeroLote, int quantidade);
}
