package com.nano.keycloak_engine.service;

import java.util.Collections;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;

/**
 * Serviço de altomação que tem o poder de fazer em milesegundos 
 * o que um desenvolvedor demora minutos  fazendo clics no navegador. 
 */

@Service
@RequiredArgsConstructor 
public class KeycloakService {

    private final Keycloak keycloak;

    public void criarUsuario(String realm, String username, String email, String senha, String roleName){

        // 1. Definir as credenciais (senha)
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(senha);
        credential.setTemporary(false);

        // 2. Definir o usuário
        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        user.setEmail(email);
        user.setEnabled(true);
        user.setCredentials(Collections.singletonList(credential));

        // 3. Salvar no Keycloak
        UsersResource usersResource = keycloak.realm(realm).users();
        Response response = usersResource.create(user);

        if(response.getStatus() == 201){

             // 4. Se criou com sucesso, vamos atribuir a Role
            String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");

            // Busca a role no realm (ela precisa existir)
            RoleRepresentation role = keycloak.realm(realm).roles().get(roleName).toRepresentation();

            // Atribui a role ao usuário
            usersResource.get(userId).roles().realmLevel().add(Collections.singletonList(role));
            
            System.out.println(">>> Usuário " + username + " criado com a role " + roleName);
            
        }else{
            System.err.println(">>> Erro ao criar usuário. Status: " + response.getStatus());
        }

    }

}
