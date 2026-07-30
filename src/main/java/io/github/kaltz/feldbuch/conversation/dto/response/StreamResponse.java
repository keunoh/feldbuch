package io.github.kaltz.feldbuch.conversation.dto.response;

public record StreamResponse(
        StreamType type,
        String content
) {

    public static StreamResponse token(String content) {
        return new StreamResponse(
                StreamType.TOKEN,
                content
        );
    }

    public static StreamResponse complete() {
        return new StreamResponse(
                StreamType.COMPLETE,
                null
        );
    }

    public static StreamResponse error(String message) {
        return new StreamResponse(
                StreamType.ERROR,
                message
        );
    }
}
