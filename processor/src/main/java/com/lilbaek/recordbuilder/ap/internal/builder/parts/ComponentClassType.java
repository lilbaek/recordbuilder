package com.lilbaek.recordbuilder.ap.internal.builder.parts;

import com.squareup.javapoet.TypeName;

import javax.lang.model.element.AnnotationMirror;
import java.util.List;

public class ComponentClassType {
    public final TypeName typeName;
    public final String name;
    public final List<? extends AnnotationMirror> accessorAnnotations;
    public final List<? extends AnnotationMirror> canonicalConstructorAnnotations;

    public ComponentClassType(final TypeName typeName, final String name, final List<? extends AnnotationMirror> accessorAnnotations, final List<? extends AnnotationMirror> canonicalConstructorAnnotations) {
        this.typeName = typeName;
        this.name = name;
        this.accessorAnnotations = accessorAnnotations;
        this.canonicalConstructorAnnotations = canonicalConstructorAnnotations;
    }
}
