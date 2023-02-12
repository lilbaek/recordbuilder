package com.lilbaek.recordbuilder.ap.internal.builder.parts;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import javax.lang.model.element.Modifier;
import com.lilbaek.recordbuilder.ap.internal.builder.RecordType;

import static com.lilbaek.recordbuilder.ap.internal.builder.Builder.generatedRecordBuilderAnnotation;
import static com.lilbaek.recordbuilder.ap.internal.builder.parts.NestedClassBuilder.WITH_CLASS_PREFIX;

public class StaticBuilder {

    private static final String FROM_METHOD_NAME = "from";
    private static final String FROM_WITH_RECORD_NAME = "_With";
    private static final String COPY_METHOD_NAME = "builder";
    private static final String BUILDER_METHOD_NAME = "builder";

    private StaticBuilder() {
    }

    public static void buildStaticParts(final TypeSpec.Builder builder, final RecordType record) {
        staticDefaultBuilderPart(builder, record);
        staticCopyBuilderPart(builder, record);
        staticFromWithPart(builder, record);
    }

    /**
     * Builds
     * public static AnnotatedBuilder builder() {
     * return new AnnotatedBuilder();
     * }
     */
    private static void staticDefaultBuilderPart(final TypeSpec.Builder builder, final RecordType record) {
        final var methodSpec = MethodSpec.methodBuilder(BUILDER_METHOD_NAME)
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .addTypeVariables(record.typeVariables)
                        .returns(record.builderRecordClassType.typeName)
                        .addStatement("return new $T()", record.builderRecordClassType.typeName)
                        .build();
        builder.addMethod(methodSpec);
    }

    /**
     * Builds
     * public static AnnotatedBuilder builder(Annotated from) {
     * return new AnnotatedBuilder(from.start(), from.end(), from.difference(), from.message());
     * }
     */
    private static void staticCopyBuilderPart(final TypeSpec.Builder builder, final RecordType record) {
        final var codeBuilder = CodeBlock.builder().add("return new $T(", record.builderRecordClassType.typeName);
        final int bound = record.recordComponents.size();
        for (int index = 0; index < bound; index++) {
            if (index > 0) {
                codeBuilder.add(", ");
            }
            codeBuilder.add("from.$L()", record.recordComponents.get(index).name);
        }
        codeBuilder.add(")");

        final var methodSpec = MethodSpec.methodBuilder(COPY_METHOD_NAME)
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .addTypeVariables(record.typeVariables)
                        .addParameter(record.recordClassType.typeName, "from")
                        .returns(record.builderRecordClassType.typeName)
                        .addStatement(codeBuilder.build())
                        .build();
        builder.addMethod(methodSpec);
    }

    /**
     * Builds
     * public static AnnotatedBuilder.With from(Annotated from) {
     * return new _FromWith(from);
     * }
     */
    private static void staticFromWithPart(final TypeSpec.Builder builder, final RecordType record) {
        fromWithClassPart(builder, record);

        final var methodSpec = MethodSpec.methodBuilder(FROM_METHOD_NAME)
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .addTypeVariables(record.typeVariables)
                        .addParameter(record.recordClassType.typeName, FROM_METHOD_NAME)
                        .returns(buildWithTypeName(record))
                        .addStatement("return new $L$L(from)", FROM_WITH_RECORD_NAME, record.typeVariables.isEmpty() ? "" : "<>")
                        .build();
        builder.addMethod(methodSpec);
    }

    /**
     * Builds
     * private static final class _FromWith implements AnnotatedBuilder.With {
     * .....
     * }
     */
    private static void fromWithClassPart(final TypeSpec.Builder builder, final RecordType record) {
        final var fromWithClassBuilder = TypeSpec.classBuilder(FROM_WITH_RECORD_NAME)
                        .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                        .addAnnotation(generatedRecordBuilderAnnotation)
                        .addTypeVariables(record.typeVariables)
                        .addSuperinterface(buildWithTypeName(record));
        fromWithClassBuilder.addField(record.recordClassType.typeName, "from", Modifier.PRIVATE, Modifier.FINAL);
        final MethodSpec constructorSpec = MethodSpec.constructorBuilder()
                        .addParameter(record.recordClassType.typeName, "from")
                        .addStatement("this.from = from")
                        .addModifiers(Modifier.PRIVATE)
                        .build();
        fromWithClassBuilder.addMethod(constructorSpec);

        final int bound = record.recordComponents.size();
        for (int index = 0; index < bound; index++) {
            final var component = record.recordComponents.get(index);
            final MethodSpec methodSpec = MethodSpec.methodBuilder(component.name)
                            .returns(component.typeName)
                            .addAnnotation(Override.class)
                            .addModifiers(Modifier.PUBLIC)
                            .addStatement("return from.$L()", component.name)
                            .build();
            fromWithClassBuilder.addMethod(methodSpec);
        }
        builder.addType(fromWithClassBuilder.build());
    }

    private static TypeName buildWithTypeName(final RecordType record) {

        final ClassName rawTypeName = ClassName.get(record.packageName, record.builderRecordClassType.name + "." + WITH_CLASS_PREFIX);
        if (record.typeVariables.isEmpty()) {
            return rawTypeName;
        }
        return ParameterizedTypeName.get(rawTypeName, record.typeVariables.toArray(new TypeName[] {}));
    }
}
