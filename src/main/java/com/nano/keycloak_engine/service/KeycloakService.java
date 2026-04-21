package com.nano.keycloak_engine.service;

import java.util.Collections;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import jakarta.ws.rs.NotFoundException;
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

        // 1. Validação de Segurança da Senha (Mínimo 8 caracteres)
        if(senha == null || senha.length() < 8){
            throw new IllegalArgumentException("Segurança: A senha deve ter no mínimo 8 caracteres!");
        }

        // 2. Automação: Garante que a Role existe no Realm antes de criar o usuário
        verificarOuCriarRole(realm, roleName);

        // 3. Definir as credenciais (senha)
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(senha);
        credential.setTemporary(false);

        // 4. Definir o usuário
        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        user.setEmail(email);
        user.setEmailVerified(true);
        user.setEnabled(true);
        user.setCredentials(Collections.singletonList(credential));

        // 5. Salvar no Keycloak
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

     /**
     * Método privado para garantir a existência da Role.
     * Se não existir, o "Cérebro" a cria automaticamente.
     */
    private void verificarOuCriarRole(String realm, String roleName){

        RolesResource rolesResource = keycloak.realm(realm).roles();

        try {
            // Tenta buscar a role no Keycloak
            rolesResource.get(roleName).toRepresentation();
        } catch (NotFoundException e) {
            // Caso não encontre (404), o Cérebro toma a decisão de criar
            System.out.println(">>> Automação: Role '" + roleName + "' não encontrada. Criando automaticamente...");

            RoleRepresentation newRole = new RoleRepresentation();
            newRole.setName(roleName);
            newRole.setDescription("Role gerada automaticamente pelo Keycloak Engine");
            
            rolesResource.create(newRole);

            if(roleName.equalsIgnoreCase("ADMIN")) {
                System.out.println("⚠️ Alerta: Role administrativa (ADMIN) foi provisionada via API.");
            }
        }

    }

}
