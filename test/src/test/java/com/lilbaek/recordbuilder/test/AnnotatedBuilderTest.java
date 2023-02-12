package com.lilbaek.recordbuilder.test;

import org.junit.jupiter.api.Test;

import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnnotatedBuilderTest {

    @Test
    void testBuilderMethod() {
        final var instance = AnnotatedBuilder.builder()
                        .start(0)
                        .end(2)
                        .difference(1)
                        .message(Optional.of("message"))
                        .build();
        assertEquals(0, instance.start());
        assertEquals(2, instance.end());
        assertEquals(1, instance.difference());
        assertEquals("message", instance.message().get());
    }

    @Test
    void testBuilderMethodDefaults() {
        final var instance = AnnotatedBuilder.builder()
                        .start(2)
                        .build();
        assertEquals(2, instance.start());
        assertEquals(0, instance.end());
        assertEquals(0, instance.difference());
        assertTrue(instance.message().isEmpty());
    }

}
