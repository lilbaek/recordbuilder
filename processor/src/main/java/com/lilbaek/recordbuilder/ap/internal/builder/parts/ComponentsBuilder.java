package com.lilbaek.recordbuilder.ap.internal.builder.parts;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import javax.lang.model.element.Modifier;
import com.lilbaek.recordbuilder.ap.internal.builder.RecordType;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

public class ComponentsBuilder {
    private static final TypeName optionalType = TypeName.get(Optional.class);
    private static final TypeName optionalIntType = TypeName.get(OptionalInt.class);
    private static final TypeName optionalLongType = TypeName.get(OptionalLong.class);
    private static final TypeName optionalDoubleType = TypeName.get(OptionalDouble.class);

    private ComponentsBuilder() {
    }

    /**
     * Builds a field + a setter method for all components
     */
    public static void addComponentParts(final TypeSpec.Builder builder, final RecordType record) {
        record.recordComponents.forEach(component -> {
            fieldPart(builder, component);
            setterMethodPart(builder, record, component);
        });
    }

    /**
     * Builds:
     * private float start;
     */
    private static void fieldPart(final TypeSpec.Builder builder, final ComponentClassType component) {
        final var fieldSpecBuilder = FieldSpec.builder(component.typeName, component.name, Modifier.PRIVATE);
        final var thisOptionalType = optionalFromComponent(component);
        if (thisOptionalType.isPresent()) {
            final var codeBlock = CodeBlock.builder()
                            .add("$T.empty()", thisOptionalType.get().typeName())
                            .build();
            fieldSpecBuilder.initializer(codeBlock);
        }
        builder.addField(fieldSpecBuilder.build());
    }

    /**
     * Builds:
     * public AnnotatedBuilder start(float start) {
     *         this.start = start;
     *         return this;
     * }
     */
    private static void setterMethodPart(final TypeSpec.Builder builder, final RecordType record, final ComponentClassType component) {
        final var methodSpec = MethodSpec.methodBuilder(component.name)
                        .addModifiers(Modifier.PUBLIC)
                        .addStatement("this.$L = $L", component.name, component.name)
                        .returns(record.builderRecordClassType.typeName);

        final var parameterSpecBuilder = ParameterSpec.builder(component.typeName, component.name);
        BuilderHelpers.constructorAnnotationsPart(component, parameterSpecBuilder);
        methodSpec.addStatement("return this").addParameter(parameterSpecBuilder.build());
        builder.addMethod(methodSpec.build());
    }

    private static boolean isOptional(final ComponentClassType component) {
        if (component.typeName.equals(optionalType)) {
            return true;
        }
        return (component.typeName instanceof ParameterizedTypeName parameterizedTypeName)
                        && parameterizedTypeName.rawType.equals(optionalType);
    }

    static Optional<OptionalWrapper> optionalFromComponent(final ComponentClassType component) {
        if (component.typeName.equals(optionalIntType)) {
            return Optional.of(new OptionalWrapper(optionalIntType, TypeName.get(int.class)));
        }
        if (component.typeName.equals(optionalLongType)) {
            return Optional.of(new OptionalWrapper(optionalLongType, TypeName.get(long.class)));
        }
        if (component.typeName.equals(optionalDoubleType)) {
            return Optional.of(new OptionalWrapper(optionalDoubleType, TypeName.get(double.class)));
        }
        if (isOptional(component)) {
            if (!(component.typeName instanceof ParameterizedTypeName parameterizedType)) {
                return Optional.of(new OptionalWrapper(optionalType, TypeName.get(Object.class)));
            }
            final TypeName containingType = parameterizedType.typeArguments.isEmpty()
                            ? TypeName.get(Object.class)
                            : parameterizedType.typeArguments.get(0);
            return Optional.of(new OptionalWrapper(optionalType, containingType));
        }
        return Optional.empty();
    }

    record OptionalWrapper(TypeName typeName, TypeName valueType) {

    }
}
