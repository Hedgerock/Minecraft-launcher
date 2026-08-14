package com.launcher.game.exception;

public class GameLaunchException extends RuntimeException {
    public GameLaunchException(String message) {
        super(message);
    }


    public GameLaunchException(String message, Throwable cause) {
        super(message, cause);
    }

}
