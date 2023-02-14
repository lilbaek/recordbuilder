package com.lilbaek.recordbuilder.ap.internal.builder;

import com.lilbaek.recordbuilder.ap.internal.AnnotationProcessorContext;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;

public class BuilderWriter {

    public static final String INDENT = "    ";

    public static void writeRecord(final BuilderResult result, final AnnotationProcessorContext annotationProcessorContext) {
        final var javaFile = javaFileBuilder(result.record().packageName, result.result());
        final var filer = annotationProcessorContext.filer();
        try {
            final var fullyQualifiedName = result.packageName().isEmpty() ? result.builderClassType().name : (result.packageName() + "." + result.builderClassType().name);
            final var sourceFile = filer.createSourceFile(fullyQualifiedName, result.record().originalRecordType);
            try (final var writer = sourceFile.openWriter()) {
                javaFile.writeTo(writer);
            }
        } catch (final IOException e) {
            annotationProcessorContext
                            .printError(String.format("Could not create source file - %s, %s", e.getMessage() != null ? e.getMessage() : "",
                                            result.record().originalRecordType));
        }
    }

    private static JavaFile javaFileBuilder(final String packageName, final TypeSpec type) {
        final var javaFileBuilder = JavaFile.builder(packageName, type).skipJavaLangImports(true).indent(INDENT);
        return javaFileBuilder.build();
    }
}
