package com.lilbaek.recordbuilder.test;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AnnotatedWitherTest {

    @Test
    void testExisting() {
        final var instance = AnnotatedBuilder.builder()
                        .start(0)
                        .end(2)
                        .difference(1)
                        .message(Optional.of("message"))
                        .build();

        final var newInstance = AnnotatedBuilder.from(instance)
                        .withMessage(Optional.empty());

        assertEquals(0, newInstance.start());
        assertEquals(2, newInstance.end());
        assertEquals(1, newInstance.difference());
        assertTrue(newInstance.message().isEmpty());
    }
}
