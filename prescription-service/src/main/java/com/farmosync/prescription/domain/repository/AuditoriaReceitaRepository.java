package com.farmosync.prescription.domain.repository;

import com.farmosync.prescription.domain.model.AuditoriaReceita;
import java.util.Optional;

public interface AuditoriaReceitaRepository {
    AuditoriaReceita salvar(AuditoriaReceita auditoria);
    Optional<AuditoriaReceita> buscarPorId(String id);
    Optional<AuditoriaReceita> buscarPorVendaId(String vendaId);
}
