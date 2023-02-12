package com.lilbaek.recordbuilder.ap.internal.builder;

import com.lilbaek.recordbuilder.ap.internal.builder.parts.ComponentClassType;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class RecordType {
    public final String uniqueVarName;
    public final RecordClassType recordClassType;
    public final String packageName;
    public final RecordClassType builderRecordClassType;
    public final List<TypeVariableName> typeVariables;
    public final List<ComponentClassType> recordComponents;
    public final TypeElement originalRecordType;
    public static final String CLASS_SUFFIX = "Builder";
    public RecordType(final TypeElement typeElement) {
        originalRecordType = typeElement;
        recordClassType = getRecordClassType(originalRecordType, originalRecordType.getTypeParameters());
        packageName = getPackageName(typeElement);
        builderRecordClassType = getRecordClassType(packageName, getBuilderName(originalRecordType, recordClassType),
                        originalRecordType.getTypeParameters());
        typeVariables = originalRecordType.getTypeParameters().stream().map(TypeVariableName::get).collect(Collectors.toList());
        recordComponents = buildRecordComponents(originalRecordType);
        uniqueVarName = getUniqueVarName("");
    }

    private List<ComponentClassType> buildRecordComponents(final TypeElement record) {
        final var accessorAnnotations = record.getRecordComponents().stream().map(e -> e.getAccessor().getAnnotationMirrors()).toList();
        final var canonicalConstructorAnnotations = findCanonicalConstructor(record)
                        .map(constructor -> ((ExecutableElement) constructor).getParameters().stream().map(
                                        Element::getAnnotationMirrors).collect(Collectors.toList())).orElse(List.of());
        final var recordComponents = record.getRecordComponents();

        final int bound = recordComponents.size();
        final List<ComponentClassType> result = new ArrayList<>();
        for (int index = 0; index < bound; index++) {
            final var thisAccessorAnnotations = (accessorAnnotations.size() > index) ?
                            accessorAnnotations.get(index) :
                            List.<AnnotationMirror>of();
            final var thisCanonicalConstructorAnnotations = (canonicalConstructorAnnotations.size() > index) ?
                            canonicalConstructorAnnotations.get(index) :
                            List.<AnnotationMirror>of();
            result.add(getComponentClassType(recordComponents.get(index), thisAccessorAnnotations,
                            thisCanonicalConstructorAnnotations));
        }
        return result;
    }

    private String getUniqueVarName(final String prefix) {
        final var name = prefix + "r";
        final var alreadyExists = recordComponents.stream()
                        .map(x -> x.name)
                        .anyMatch(n -> n.equals(name));
        return alreadyExists ? getUniqueVarName(prefix + "_") : name;
    }

    public boolean builderIsInPackage() {
        return getPackageName(originalRecordType).equals(packageName);
    }

    private static String getBuilderName(final TypeElement element, final RecordClassType recordClassType) {
        final var baseName = recordClassType.name + CLASS_SUFFIX;
        return getBuilderNamePrefix(element.getEnclosingElement()) + baseName;
    }

    private static String getBuilderNamePrefix(final Element element) {
        if (element instanceof TypeElement) {
            return getBuilderNamePrefix(element.getEnclosingElement()) + element.getSimpleName().toString();
        }
        return "";
    }

    private static RecordClassType getRecordClassType(final String packageName, final String simpleName,
                    final List<? extends TypeParameterElement> typeParameters) {
        return getRecordClassType(ClassName.get(packageName, simpleName), typeParameters);
    }

    private static RecordClassType getRecordClassType(final TypeElement typeElement, final List<? extends TypeParameterElement> typeParameters) {
        return getRecordClassType(ClassName.get(typeElement), typeParameters);
    }

    private static RecordClassType getRecordClassType(final ClassName builderClassName, final List<? extends TypeParameterElement> typeParameters) {
        if (typeParameters.isEmpty()) {
            return new RecordClassType(builderClassName, builderClassName.simpleName());
        }
        final TypeName[] typeNames = typeParameters.stream().map(TypeVariableName::get).toArray(TypeName[]::new);
        return new RecordClassType(ParameterizedTypeName.get(builderClassName, typeNames), builderClassName.simpleName());
    }

    private static String getPackageName(final TypeElement typeElement) {
        final String name = getTypeElementWithNesting(typeElement).getQualifiedName().toString();
        final int index = name.lastIndexOf(".");
        return (index > -1) ? name.substring(0, index) : "";
    }

    private static TypeElement getTypeElementWithNesting(TypeElement typeElement) {
        while (typeElement.getNestingKind().isNested()) {
            final Element enclosingElement = typeElement.getEnclosingElement();
            if (enclosingElement instanceof TypeElement) {
                typeElement = (TypeElement) enclosingElement;
            } else {
                break;
            }
        }
        return typeElement;
    }

    private static ComponentClassType getComponentClassType(final RecordComponentElement recordComponent,
                    final List<? extends AnnotationMirror> accessorAnnotations,
                    final List<? extends AnnotationMirror> canonicalConstructorAnnotations) {
        final var typeName = TypeName.get(recordComponent.asType());
        return new ComponentClassType(typeName, recordComponent.getSimpleName().toString(), accessorAnnotations,
                        canonicalConstructorAnnotations);
    }

    private static Optional<? extends Element> findCanonicalConstructor(final TypeElement record) {
        if (record.getKind() != ElementKind.RECORD) {
            return Optional.empty();
        }
        final var componentList = record.getRecordComponents().stream().map(e -> e.asType().toString()).toList();
        return record.getEnclosedElements().stream()
                        .filter(element -> element.getKind() == ElementKind.CONSTRUCTOR)
                        .filter(element -> {
                            final var parameters = ((ExecutableElement) element).getParameters();
                            final var parametersList = parameters.stream().map(e -> e.asType().toString()).toList();
                            return componentList.equals(parametersList);
                        })
                        .findFirst();
    }
}
