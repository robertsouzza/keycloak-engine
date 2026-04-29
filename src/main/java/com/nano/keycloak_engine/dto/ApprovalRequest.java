package com.nano.keycloak_engine.dto;

import java.util.UUID;

public record ApprovalRequest(
    UUID id,
    String type, // "REALM" ou "ADMIN_ROLE"
    String realm,
    String username,
    String email,
    String password,
    String role,
    String status // "PENDING", "APPROVED", "REJECTED"
) {

}
