package io.github.kaltz.feldbuch.ai.model;

public record TitleCommand(
        String message
) {

    public static TitleCommand from(String message) {
        return new TitleCommand(message);
    }
}
