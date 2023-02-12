package com.lilbaek.recordbuilder.ap.internal.builder;

import com.lilbaek.recordbuilder.ap.internal.builder.parts.ClassBuilder;
import com.lilbaek.recordbuilder.ap.internal.builder.parts.ComponentsBuilder;
import com.lilbaek.recordbuilder.ap.internal.builder.parts.NestedClassBuilder;
import com.lilbaek.recordbuilder.ap.internal.builder.parts.StaticBuilder;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.TypeSpec;
import com.lilbaek.recordbuilder.RecordBuilder;

import javax.annotation.processing.Generated;

public class Builder {
    public static final AnnotationSpec generatedRecordBuilderAnnotation = AnnotationSpec.builder(Generated.class)
                    .addMember("value", "$S", RecordBuilder.class.getName()).build();
    public BuilderResult build(final RecordType record) {
        final TypeSpec.Builder builder = TypeSpec.classBuilder(record.builderRecordClassType.name)
                        .addAnnotation(generatedRecordBuilderAnnotation)
                        .addTypeVariables(record.typeVariables);

        ClassBuilder.buildClassParts(builder, record);
        ComponentsBuilder.addComponentParts(builder, record);
        StaticBuilder.buildStaticParts(builder, record);
        NestedClassBuilder.buildNestedClassParts(builder, record);
        return new BuilderResult(record, builder.build());
    }
}
