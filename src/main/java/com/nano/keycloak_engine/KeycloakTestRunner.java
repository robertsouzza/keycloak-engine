package com.nano.keycloak_engine;

import com.nano.keycloak_engine.service.KeycloakService; // Importe o seu serviço
import org.keycloak.admin.client.Keycloak;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class KeycloakTestRunner implements CommandLineRunner {

    private final Keycloak keycloak;
    private final KeycloakService keycloakService; // Adicionamos o serviço aqui

    // O Spring injeta ambos automaticamente via construtor
    public KeycloakTestRunner(Keycloak keycloak, KeycloakService keycloakService) {
        this.keycloak = keycloak;
        this.keycloakService = keycloakService;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println(">>> INICIANDO TESTE DE CONEXÃO E CRIAÇÃO...");
        
        try {
            // Teste 1: Listar Realms (o que já fazíamos)
            var realms = keycloak.realms().findAll();
            System.out.println(">>> CONEXÃO OK! Realms encontrados: " + realms.size());
            
            // Teste 2: Tentar criar um usuário de verdade usando o nosso "Cérebro"
            // IMPORTANTE: O realm 'demo-realm' e a role 'USER_ROLE' precisam existir no Keycloak
            System.out.println(">>> TENTANDO CRIAR USUÁRIO NO 'demo-realm'...");
            keycloakService.criarUsuario(
                "demo-realm", 
                "roberto_teste", 
                "roberto@nano.com", 
                "123456", 
                "USER_ROLE"
            );
            
            System.out.println(">>> PROCESSO FINALIZADO COM SUCESSO!");
            
        } catch (Exception e) {
            System.err.println(">>> ERRO NO PROCESSO: " + e.getMessage());
        }
    }
}
