package com.nano.keycloak_engine.controller;

import com.nano.keycloak_engine.dto.UserRequest;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor; 

import com.nano.keycloak_engine.service.KeycloakService;

/**
 * api REST CONTROLLER - responsável em pedir a criação de usuários. 
 */

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserRegistrationController {

    private final KeycloakService keycloakService;

    @PostMapping("/create")
    public String register(@RequestBody UserRequest request) {
        keycloakService.criarUsuario(
            "demo-realm", 
            request.username(), 
            request.email(), 
            request.password(), 
            "USER_ROLE"
        );
        return "Solicitação processada para o usuário: " + request.username();
    }
}

