package com.nano.keycloak_engine.controller;

public record UserRequest(
    String username,
    String email,
    String password
) {

}
