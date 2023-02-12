package com.lilbaek.recordbuilder.ap.internal.builder.parts;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import javax.lang.model.element.Modifier;
import com.lilbaek.recordbuilder.ap.internal.builder.RecordType;

import java.util.function.Predicate;

import static com.lilbaek.recordbuilder.ap.internal.builder.Builder.generatedRecordBuilderAnnotation;

/**
 * Builds the nested class for withers
 */
public class NestedClassBuilder {
    private static final String WITH_METHOD_PREFIX = "with";
    public static final String WITH_CLASS_PREFIX = "With";
    private static final TypeName validTypeJavax = ClassName.get("javax.validation", "Valid");
    private static final TypeName validTypeJakarta = ClassName.get("jakarta.validation", "Valid");

    private NestedClassBuilder() {
    }

    /**
     * Builds
     * public class AnnotatedBuilder {
     *  public interface With {
     *      ..content..
     *  }
     * }
     */
    public static void buildNestedClassParts(final TypeSpec.Builder builder, final RecordType record) {
        final var classBuilder = TypeSpec.interfaceBuilder(WITH_CLASS_PREFIX)
                        .addAnnotation(generatedRecordBuilderAnnotation)
                        .addModifiers(Modifier.PUBLIC)
                        .addTypeVariables(record.typeVariables);
        record.recordComponents.forEach(component -> addNestedGetterMethodPart(classBuilder, component, component.name));
        final int bound = record.recordComponents.size();
        for (int index = 0; index < bound; index++) {
            addSingleWithMethodPart(classBuilder, record, record.recordComponents.get(index), index);
        }
        builder.addType(classBuilder.build());
    }

    /**
     * Builds
     * default Annotated withStart(float start) {
     *     return new Annotated(start, end(), difference());
     * }
     */
    private static void addSingleWithMethodPart(final TypeSpec.Builder classBuilder, final RecordType record, final ComponentClassType component,
                    final int index) {
        final var codeBlockBuilder = CodeBlock.builder();
        codeBlockBuilder.add("$[return ");
        codeBlockBuilder.add("new $T(", record.recordClassType.typeName);
        addComponentCallsAsArguments(record, index, codeBlockBuilder);
        codeBlockBuilder.add(")");
        codeBlockBuilder.add(";$]");

        final var methodName = getWithMethodName(component);
        final var parameterSpecBuilder = ParameterSpec.builder(component.typeName, component.name);
        BuilderHelpers.constructorAnnotationsPart(component, parameterSpecBuilder);
        final var methodSpec = MethodSpec.methodBuilder(methodName)
                        .addModifiers(Modifier.PUBLIC, Modifier.DEFAULT)
                        .addParameter(parameterSpecBuilder.build())
                        .addCode(codeBlockBuilder.build())
                        .returns(record.recordClassType.typeName)
                        .build();
        classBuilder.addMethod(methodSpec);
    }


    /**
     * Builds
     * float start();
     */
    private static void addNestedGetterMethodPart(final TypeSpec.Builder classBuilder, final ComponentClassType component, final String methodName) {
        final var methodSpecBuilder = MethodSpec.methodBuilder(methodName)
                        .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
                        .returns(component.typeName);
        addAccessorAnnotations(component, methodSpecBuilder, NestedClassBuilder::filterOutValid);
        classBuilder.addMethod(methodSpecBuilder.build());
    }

    private static String getWithMethodName(final ComponentClassType component) {
        final var name = component.name;
        if (name.length() == 1) {
            return WITH_METHOD_PREFIX + name.toUpperCase();
        }
        return WITH_METHOD_PREFIX + Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    private static boolean filterOutValid(final AnnotationSpec annotationSpec) {
        return !annotationSpec.type.equals(validTypeJakarta) && !annotationSpec.type.equals(validTypeJavax);
    }

    private static void addAccessorAnnotations(final ComponentClassType component, final MethodSpec.Builder methodSpecBuilder,
                    final Predicate<AnnotationSpec> additionalFilter) {
        component.accessorAnnotations
                        .stream()
                        .map(AnnotationSpec::get)
                        .filter(BuilderHelpers::filterOutOverrideAnnotation)
                        .filter(additionalFilter)
                        .forEach(methodSpecBuilder::addAnnotation);

    }

    private static void addComponentCallsAsArguments(final RecordType record, final int index, final CodeBlock.Builder codeBlockBuilder) {
        final int bound = record.recordComponents.size();
        for (int parameterIndex = 0; parameterIndex < bound; parameterIndex++) {
            if (parameterIndex > 0) {
                codeBlockBuilder.add(", ");
            }
            final ComponentClassType parameterComponent = record.recordComponents.get(parameterIndex);
            if (parameterIndex == index) {
                codeBlockBuilder.add("$L", parameterComponent.name);
            } else {
                codeBlockBuilder.add("$L()", parameterComponent.name);
            }
        }
    }
}
