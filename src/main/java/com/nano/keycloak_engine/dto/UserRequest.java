package com.nano.keycloak_engine.dto;

public record UserRequest(
    String username,
    String email,
    String password,
    String role
) {

}
