package com.lilbaek.recordbuilder.ap.internal.builder.parts;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import javax.lang.model.element.Modifier;
import com.lilbaek.recordbuilder.ap.internal.builder.RecordType;

import java.util.Objects;
import java.util.stream.Collectors;

public class ClassBuilder {
    public static final String BUILD_METHOD_NAME = "build";

    private ClassBuilder() {
    }

    /**
     * Builds the class part of the record builder
     */
    public static void buildClassParts(final TypeSpec.Builder builder, final RecordType record) {
        setVisibility(builder, record);
        defaultConstructorPart(builder);
        allArgsConstructorPart(builder, record);
        addBuildMethod(builder, record);
        hashCodePart(builder, record);
        equalsPart(builder, record);
        toStringPart(builder, record);
    }

    /**
     * Sets visibility based on location of the record vs the record builder
     */
    private static void setVisibility(final TypeSpec.Builder builder, final RecordType record) {
        final var modifiers = record.originalRecordType.getModifiers();
        if (record.builderIsInPackage()) {
            if (modifiers.contains(Modifier.PUBLIC) || modifiers.contains(Modifier.PRIVATE) || modifiers.contains(Modifier.PROTECTED)) {
                builder.addModifiers(Modifier.PUBLIC);  // builders are top level classes - can only be public or package-private
            }
        } else {
            builder.addModifiers(Modifier.PUBLIC);
        }
    }

    /**
     * Builds
     * private AnnotatedBuilder() {
     * }
     */
    private static void defaultConstructorPart(final TypeSpec.Builder builder) {
        final var constructor = MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PRIVATE)
                        .build();
        builder.addMethod(constructor);
    }

    /**
     * Builds
     * private AnnotatedBuilder(float start, float end, float difference) {
     *   this.start = start;
     *   this.end = end;
     *   this.difference = difference;
     * }
     */
    private static void allArgsConstructorPart(final TypeSpec.Builder builder, final RecordType record) {
        if (record.recordComponents.size() > 0) {
            final var constructorBuilder = MethodSpec.constructorBuilder()
                            .addModifiers(Modifier.PRIVATE);
            record.recordComponents.forEach(component -> {
                constructorBuilder.addParameter(component.typeName, component.name);
                constructorBuilder.addStatement("this.$L = $L", component.name, component.name);
            });
            builder.addMethod(constructorBuilder.build());
        }
    }

    /**
     * Builds     *
     * public int hashCode() {
     *     return Objects.hash(start, end, difference);
     * }
     */
    private static void hashCodePart(final TypeSpec.Builder builder, final RecordType record) {
        final var codeBuilder = CodeBlock.builder().add("return $T.hash(", Objects.class);
        BuilderHelpers.buildArgumentsPart(record, codeBuilder);

        final var methodSpec = MethodSpec.methodBuilder("hashCode")
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(Override.class)
                        .returns(TypeName.INT)
                        .addStatement(codeBuilder.build())
                        .build();
        builder.addMethod(methodSpec);
    }

    /**
     * Builds     *
     * public boolean equals(Object o) {
     *     return (this == o) || ((o instanceof AnnotatedBuilder r)
     *             && (start == r.start)
     *             && (end == r.end)
     *             && (difference == r.difference));
     * }
     */
    private static void equalsPart(final TypeSpec.Builder builder, final RecordType record) {
        final var uniqueVarName = record.uniqueVarName;
        final var codeBuilder = CodeBlock.builder();
        codeBuilder.add("return (this == o) || (");
        if (record.typeVariables.isEmpty()) {
            codeBuilder.add("(o instanceof $L $L)", record.builderRecordClassType.name, uniqueVarName);
        } else {
            final String wildcardList = record.typeVariables.stream().map(__ -> "?").collect(Collectors.joining(","));
            codeBuilder.add("(o instanceof $L<$L> $L)", record.builderRecordClassType.name, wildcardList, uniqueVarName);
        }
        record.recordComponents.forEach(recordComponent -> {
            final String name = recordComponent.name;
            if (recordComponent.typeName.isPrimitive()) {
                codeBuilder.add("\n&& ($L == $L.$L)", name, uniqueVarName, name);
            } else {
                codeBuilder.add("\n&& $T.equals($L, $L.$L)", Objects.class, name, uniqueVarName, name);
            }
        });
        codeBuilder.add(")");

        final var methodSpec = MethodSpec.methodBuilder("equals")
                        .addParameter(Object.class, "o")
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(Override.class)
                        .returns(TypeName.BOOLEAN)
                        .addStatement(codeBuilder.build())
                        .build();
        builder.addMethod(methodSpec);
    }

    /**
     * Builds
     * public String toString() {
     *     return "AnnotatedBuilder[start=" + start + ", end=" + end + ", difference=" + difference + "]";
     * }
     */
    private static void toStringPart(final TypeSpec.Builder builder, final RecordType record) {
        final var codeBuilder = CodeBlock.builder().add("return \"$L[", record.builderRecordClassType.name);
        final int bound = record.recordComponents.size();
        for (int index = 0; index < bound; index++) {
            if (index > 0) {
                codeBuilder.add(", ");
            }
            final String name = record.recordComponents.get(index).name;
            codeBuilder.add("$L=\" + $L + \"", name, name);
        }
        codeBuilder.add("]\"");

        final var methodSpec = MethodSpec.methodBuilder("toString")
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(Override.class)
                        .returns(String.class)
                        .addStatement(codeBuilder.build())
                        .build();
        builder.addMethod(methodSpec);
    }

    /**
     * Builds
     * public Annotated build() {
     *     return new Annotated(start, end, difference);
     * }
     */
    private static void addBuildMethod(final TypeSpec.Builder builder, final RecordType record) {
        final CodeBlock codeBlock = BuilderHelpers.buildNewInstancePart(record);
        final var methodSpec = MethodSpec.methodBuilder(BUILD_METHOD_NAME)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(record.recordClassType.typeName)
                        .addCode(codeBlock)
                        .build();
        builder.addMethod(methodSpec);
    }
}
