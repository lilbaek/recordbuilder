package com.lilbaek.recordbuilder.test;

import com.lilbaek.recordbuilder.RecordBuilder;

import java.util.Optional;

@RecordBuilder
public record Annotated(float start, float end, @TestAnnotation float difference, Optional<String> message) {
}
