package com.farmosync.prescription.domain.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReceitaMedicaTest {

    @Test
    public void deveRejeitarCrmComUfInvalida() {
        assertThrows(IllegalArgumentException.class, () -> new CRM("12345", "SPF"));
        assertThrows(IllegalArgumentException.class, () -> new CRM("12345", "S"));
    }

    @Test
    public void deveRejeitarReceitaComDataNoFuturo() {
        CRM crm = new CRM("12345", "SP");
        assertThrows(IllegalArgumentException.class, () ->
            ReceitaMedica.builder()
                .crmMedico(crm)
                .nomeMedico("Dr. Drauzio")
                .dataEmissao(LocalDate.now().plusDays(1))
                .assinaturaDigital("SIG123")
                .build()
        );
    }

    @Test
    public void deveConsiderarReceitaExpiradaAposDezDias() {
        CRM crm = new CRM("12345", "SP");
        LocalDate dataEmissao = LocalDate.now().minusDays(11);
        ReceitaMedica receita = ReceitaMedica.builder()
                .crmMedico(crm)
                .nomeMedico("Dr. Drauzio")
                .dataEmissao(dataEmissao)
                .assinaturaDigital("SIG123")
                .build();

        assertTrue(receita.isExpirada(LocalDate.now()));
    }

    @Test
    public void deveConsiderarReceitaValidaDentroDoPrazoDeDezDias() {
        CRM crm = new CRM("12345", "SP");
        LocalDate dataEmissao = LocalDate.now().minusDays(5);
        ReceitaMedica receita = ReceitaMedica.builder()
                .crmMedico(crm)
                .nomeMedico("Dr. Drauzio")
                .dataEmissao(dataEmissao)
                .assinaturaDigital("SIG123")
                .build();

        assertFalse(receita.isExpirada(LocalDate.now()));
    }
}
