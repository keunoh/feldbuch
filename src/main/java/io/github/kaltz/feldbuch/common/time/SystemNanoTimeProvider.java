package io.github.kaltz.feldbuch.common.time;

import org.springframework.stereotype.Component;

@Component
public class SystemNanoTimeProvider implements NanoTimeProvider {

    @Override
    public long nanoTime() {
        return System.nanoTime();
    }

}
