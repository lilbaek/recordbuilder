package com.lilbaek.recordbuilder.ap.internal;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ErrorHandler {
    private ErrorHandler() {

    }

    public static void handleUncaughtError(final Element element, final Throwable thrown, final ProcessingEnvironment processingEnv) {
        final var sw = new StringWriter();
        thrown.printStackTrace(new PrintWriter(sw));
        final var reportableStacktrace = sw.toString().replace(System.lineSeparator(), "  ");
        processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR, "Internal error in the record builder process: " + reportableStacktrace, element);
    }
}
