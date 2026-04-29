package com.nano.keycloak_engine.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nano.keycloak_engine.dto.ApprovalRequest;
import com.nano.keycloak_engine.service.KeycloakService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/approvals")
@RequiredArgsConstructor
@Tag(name = "Administração", description = "Fluxo de aprovação para operações sensíveis")
public class ApprovalController {

    private final KeycloakService keycloakService;

    @GetMapping
    public List<ApprovalRequest> listar() {
        return keycloakService.listarPendencias();
    }

    @PostMapping("/{id}/approve")
    public String aprovar(@PathVariable UUID id) {
        keycloakService.aprovarSolicitacao(id);
        return "Operação autorizada e executada com sucesso!";
    }

}
