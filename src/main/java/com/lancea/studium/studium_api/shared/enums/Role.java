package com.lancea.studium.studium_api.shared.enums;

public enum Role {
    USER,
    ADMIN;

    public String getAuthority(){
        return "ROLE_" + this.name();
    }
}
