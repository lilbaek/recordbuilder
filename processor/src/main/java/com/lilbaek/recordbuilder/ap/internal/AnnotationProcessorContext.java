package com.lilbaek.recordbuilder.ap.internal;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

public record AnnotationProcessorContext(
                Elements elementUtils,
                Types typeUtils,
                Messager messager,
                Filer filer,
                boolean verbose) {

    public void printVerbose(final String message) {
        if(verbose) {
            messager.printMessage(Diagnostic.Kind.NOTE, "RecordBuilder: " + message);
        }
    }

    public void printError(final String message) {
        messager.printMessage(Diagnostic.Kind.ERROR, "RecordBuilder: " + message);
    }
}
