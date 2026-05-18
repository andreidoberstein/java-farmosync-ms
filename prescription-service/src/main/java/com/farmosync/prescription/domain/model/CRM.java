package com.farmosync.prescription.domain.model;

import lombok.Getter;
import java.util.Objects;

@Getter
public class CRM {
    private final String numero;
    private final String uf;

    public CRM(String numero, String uf) {
        if (numero == null || numero.isBlank()) {
            throw new IllegalArgumentException("Numero do CRM nao pode ser vazio");
        }
        if (uf == null || uf.length() != 2) {
            throw new IllegalArgumentException("UF do CRM deve conter exatamente 2 caracteres");
        }
        this.numero = numero.trim();
        this.uf = uf.trim().toUpperCase();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CRM crm = (CRM) o;
        return Objects.equals(numero, crm.numero) && Objects.equals(uf, crm.uf);
    }

    @Override
    public int hashCode() {
        return Objects.hash(numero, uf);
    }
}
