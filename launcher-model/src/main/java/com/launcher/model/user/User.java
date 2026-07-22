package com.launcher.model.user;

public final class User {

    private final UserId id;
    private final String username;
    private final UserRole role;

    public User(UserId id, String username, UserRole role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }

    public UserId getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public UserRole getRole() {
        return role;
    }
}
