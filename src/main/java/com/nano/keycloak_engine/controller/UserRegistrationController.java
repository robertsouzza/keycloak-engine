package com.nano.keycloak_engine.controller;

import com.nano.keycloak_engine.dto.UserRequest;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor; 

import com.nano.keycloak_engine.service.KeycloakService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * api REST CONTROLLER - responsável em pedir a criação de usuários. 
 */

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Endpoints para gestão de usuários e permissões") // Swagger Tag
public class UserRegistrationController {

    private final KeycloakService keycloakService;

    @Operation(summary = "Criar novo usuário com Role dinâmica", 
               description = "Registra um usuário no Keycloak. Se a Role informada não existir, o sistema a criará automaticamente.")
    @PostMapping("/create")
    public String register(@RequestBody UserRequest request) {
        keycloakService.criarUsuario(
            "demo-realm", 
            request.username(), 
            request.email(), 
            request.password(), 
            request.role()
        );
        return "Solicitação processada para o usuário: " + request.username() +
                 "' criado com a permissão '" + request.role() + "'.";
    }
}

