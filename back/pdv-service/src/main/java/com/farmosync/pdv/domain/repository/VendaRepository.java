package com.farmosync.pdv.domain.repository;

import com.farmosync.pdv.domain.model.Venda;
import java.util.Optional;

public interface VendaRepository {
    Venda salvar(Venda venda);
    Venda salvarComEvento(Venda venda, String eventPayload);
    Optional<Venda> buscarPorId(String id);
}
