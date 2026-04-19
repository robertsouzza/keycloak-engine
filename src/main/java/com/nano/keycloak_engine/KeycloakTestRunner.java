package com.nano.keycloak_engine;

import org.keycloak.admin.client.Keycloak;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class KeycloakTestRunner implements CommandLineRunner {

    private final Keycloak keycloak;

    public KeycloakTestRunner(Keycloak keycloak) {
        this.keycloak = keycloak;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println(">>> TESTANDO CONEXÃO COM KEYCLOAK...");
        try {
            var realms = keycloak.realms().findAll();
            System.out.println(">>> CONEXÃO OK! Realms encontrados: " + realms.size());
            realms.forEach(r -> System.out.println(" - Realm: " + r.getRealm()));
        } catch (Exception e) {
            System.err.println(">>> ERRO AO CONECTAR: " + e.getMessage());
        }
    }
}
