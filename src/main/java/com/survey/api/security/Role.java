package com.survey.api.security;

public enum Role {
    ADMIN("Admin"),
    RESPONDENT("Respondent");

    private final String roleName;

    Role(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleName() {
        return roleName;
    }
}
