package com.launcher.model.authentication;

public class OfflineCredentials implements Credentials {

    private final String nickname;

    public OfflineCredentials(String nickname) {
        this.nickname = nickname;
    }

    public String getNickname() {
        return nickname;
    }
}
