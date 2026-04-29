package com.nano.keycloak_engine.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import com.nano.keycloak_engine.dto.ApprovalRequest;

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

    // "Sala de Espera" para solicitações de ADMIN
    private final Map<UUID, ApprovalRequest> pendingApprovals = new ConcurrentHashMap<>();

    /**
     * Ponto de entrada da API. Decide se o usuário é criado na hora 
     * ou se vai para a fila de aprovação.
     */
    public String criarUsuario(String realm, String username, String email, String senha, String roleName){

        // 1. Validação de Segurança da Senha (Mantenho sua regra de 8 caracteres)
        if(senha == null || senha.length() < 8){
            throw new IllegalArgumentException("Segurança: A senha deve ter no mínimo 8 caracteres!");
        }

        // 2. INTERCEPTADOR: Se a role for ADMIN, não criamos agora.
        if("ADMIN".equalsIgnoreCase(roleName)) {
            UUID requestId = UUID.randomUUID();
            ApprovalRequest pending = new ApprovalRequest(
                requestId, "ADMIN_ROLE", realm, username, email, senha, roleName, "PENDING"
            );
            pendingApprovals.put(requestId, pending);
            
            System.out.println("⚠️ BLOQUEIO: Usuário ADMIN '" + username + "' aguardando liberação. ID: " + requestId);
            return "AGUARDANDO APROVAÇÃO: Perfil ADMIN detectado. Liberação necessária para o ID: " + requestId;
        }

        // 3. Se não for ADMIN, segue o fluxo normal que você já tinha
        return executarCriacaoNoKeycloak(realm, username, email, senha, roleName);
    }

    /**
     * O seu código original de criação, isolado para ser chamado 
     * tanto pelo fluxo normal quanto pelo fluxo de aprovação.
     */
    public String executarCriacaoNoKeycloak(String realm, String username, String email, String senha, String roleName) {
        
        verificarOuCriarRole(realm, roleName);

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(senha);
        credential.setTemporary(false);

        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        user.setEmail(email);
        user.setEmailVerified(true);
        user.setEnabled(true);
        user.setCredentials(Collections.singletonList(credential));

        UsersResource usersResource = keycloak.realm(realm).users();
        Response response = usersResource.create(user);

        if(response.getStatus() == 201){
            String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
            RoleRepresentation role = keycloak.realm(realm).roles().get(roleName).toRepresentation();
            usersResource.get(userId).roles().realmLevel().add(Collections.singletonList(role));
            
            System.out.println(">>> Usuário " + username + " criado com a role " + roleName);
            return "Usuário " + username + " criado com sucesso!";
        } else {
            System.err.println(">>> Erro ao criar usuário. Status: " + response.getStatus());
            return "Erro ao criar usuário no Keycloak.";
        }
    }

    // --- MÉTODOS DE GOVERNANÇA (PARA O ADMINISTRADOR) ---

    public List<ApprovalRequest> listarPendencias() {
        return new ArrayList<>(pendingApprovals.values());
    }

    public String aprovarSolicitacao(UUID id) {
        ApprovalRequest req = pendingApprovals.get(id);
        if (req == null) return "Solicitação não encontrada.";

        // Agora sim, chamamos o motor de criação para o ADMIN
        String resultado = executarCriacaoNoKeycloak(req.realm(), req.username(), req.email(), req.password(), req.role());
        
        pendingApprovals.remove(id); // Limpa da fila
        return "Solicitação aprovada! " + resultado;
    }

    private void verificarOuCriarRole(String realm, String roleName){
        RolesResource rolesResource = keycloak.realm(realm).roles();
        try {
            rolesResource.get(roleName).toRepresentation();
        } catch (NotFoundException e) {
            System.out.println(">>> Automação: Role '" + roleName + "' não encontrada. Criando automaticamente...");
            RoleRepresentation newRole = new RoleRepresentation();
            newRole.setName(roleName);
            newRole.setDescription("Role gerada automaticamente pelo Keycloak Engine");
            rolesResource.create(newRole);
        }
    }
}