package com.lilbaek.recordbuilder.ap.internal.builder.parts;

import com.lilbaek.recordbuilder.ap.internal.builder.RecordType;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;

class BuilderHelpers {
    private static final TypeName overrideType = TypeName.get(Override.class);
    public static boolean filterOutOverrideAnnotation(final AnnotationSpec annotationSpec) {
        return !annotationSpec.type.equals(overrideType);
    }

    /**
     * Adds constructor annotations. Filters out Override annotations
     */
    public static void constructorAnnotationsPart(final ComponentClassType component, final ParameterSpec.Builder parameterSpecBuilder) {
        component.canonicalConstructorAnnotations
                        .stream()
                        .map(AnnotationSpec::get)
                        .filter(BuilderHelpers::filterOutOverrideAnnotation)
                        .forEach(parameterSpecBuilder::addAnnotation);

    }

    /**
     Builds: return new AnnotatedBuilder(from.start(), from.end(), from.difference());
     */
    public static CodeBlock buildNewInstancePart(final RecordType record) {
        final var codeBuilder = CodeBlock.builder();
        codeBuilder.add("$[return ");
        codeBuilder.add("new $T(", record.recordClassType.typeName);
        buildArgumentsPart(record, codeBuilder);
        codeBuilder.add(";$]");
        return codeBuilder.build();
    }

    static void buildArgumentsPart(final RecordType record, final CodeBlock.Builder codeBuilder) {
        final int bound = record.recordComponents.size();
        for (int index = 0; index < bound; index++) {
            if (index > 0) {
                codeBuilder.add(", ");
            }
            codeBuilder.add("$L", record.recordComponents.get(index).name);
        }
        codeBuilder.add(")");
    }
}
