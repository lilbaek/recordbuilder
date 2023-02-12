package com.lilbaek.recordbuilder.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.AnnotatedElement;
import java.util.Optional;

class AnnotatedClassTest {

    @Test
    void testSetters() throws NoSuchMethodException {
        var method = AnnotatedBuilder.class.getMethod("start", Float.TYPE);
        var parameters = method.getParameters();
        Assertions.assertEquals(1, parameters.length);

        method = AnnotatedBuilder.class.getMethod("end", Float.TYPE);
        parameters = method.getParameters();
        Assertions.assertEquals(1, parameters.length);

        method = AnnotatedBuilder.class.getMethod("difference", Float.TYPE);
        parameters = method.getParameters();
        Assertions.assertEquals(1, parameters.length);
        assertAnnotationForDifference(parameters[0]);

        method = AnnotatedBuilder.class.getMethod("message", Optional.class);
        parameters = method.getParameters();
        Assertions.assertEquals(1, parameters.length);
    }

    private void assertAnnotationForDifference(final AnnotatedElement annotatedElement) {
        Assertions.assertNotNull(annotatedElement.getAnnotation(TestAnnotation.class));
    }
}
