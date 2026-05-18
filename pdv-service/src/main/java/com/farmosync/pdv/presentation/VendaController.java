package com.farmosync.pdv.presentation;

import com.farmosync.pdv.application.dto.RegistrarVendaRequest;
import com.farmosync.pdv.application.dto.VendaResponse;
import com.farmosync.pdv.application.usecase.RegistrarVendaUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/vendas")
@RequiredArgsConstructor
public class VendaController {
    private final RegistrarVendaUseCase registrarVendaUseCase;

    @PostMapping
    public ResponseEntity<VendaResponse> registrarVenda(@Valid @RequestBody RegistrarVendaRequest request) {
        VendaResponse response = registrarVendaUseCase.executar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
