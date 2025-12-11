package com.lancea.studium.studium_api.entity;

public enum Role {
    USER,
    ADMIN;

    public String getAuthority(){
        return "ROLE_" + this.name();
    }
}
