package com.lilbaek.recordbuilder.ap;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import com.lilbaek.recordbuilder.RecordBuilder;
import com.lilbaek.recordbuilder.ap.internal.AnnotationProcessorContext;
import com.lilbaek.recordbuilder.ap.internal.ErrorHandler;
import com.lilbaek.recordbuilder.ap.internal.builder.Builder;
import com.lilbaek.recordbuilder.ap.internal.builder.BuilderWriter;
import com.lilbaek.recordbuilder.ap.internal.builder.RecordType;
import com.lilbaek.recordbuilder.ap.internal.option.Options;

import java.util.Set;

@SupportedAnnotationTypes("com.lilbaek.recordbuilder.RecordBuilder")
public class Processor extends AbstractProcessor {
    private static final String RECORD_BUILDER = RecordBuilder.class.getName();
    private static final boolean ANNOTATIONS_CLAIMED_EXCLUSIVELY = false;
    protected static final String VERBOSE = "recordbuilder.verbose";
    private AnnotationProcessorContext annotationProcessorContext;
    private Builder builder;

    @Override
    public synchronized void init(final ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        final var options = createOptions();
        builder = new Builder();
        annotationProcessorContext = new AnnotationProcessorContext(
                        processingEnv.getElementUtils(),
                        processingEnv.getTypeUtils(),
                        processingEnv.getMessager(),
                        processingEnv.getFiler(),
                        options.verbose()
        );
    }

    private Options createOptions() {
        return new Options(Boolean.parseBoolean(processingEnv.getOptions().get(VERBOSE)));
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        if (!roundEnv.processingOver()) {
            processRecords(annotations, roundEnv);
        }
        return ANNOTATIONS_CLAIMED_EXCLUSIVELY;
    }

    private void processRecords(final Set<? extends TypeElement> annotations,
                    final RoundEnvironment roundEnvironment) {
        for (final TypeElement annotation : annotations) {
            //Type not supported. Exit
            if (annotation.getKind() != ElementKind.ANNOTATION_TYPE) {
                continue;
            }
            try {
                final var annotated = roundEnvironment.getElementsAnnotatedWith(annotation);
                for (final Element element : annotated) {
                    if (RECORD_BUILDER.equals(annotation.getQualifiedName().toString()) && element instanceof TypeElement typeElement) {
                        processRecord(typeElement);
                    }
                }
            } catch (final Throwable t) {
                ErrorHandler.handleUncaughtError(annotation, t, processingEnv);
            }
        }
    }

    private void processRecord(final TypeElement record) {
        if (!"RECORD".equals(record.getKind().name())) {
            annotationProcessorContext.printError("can only be used on records: " + record.getKind().name());
            return;
        }
        annotationProcessorContext.printVerbose("Creating builder for: " + record.getQualifiedName());

        final var build = builder.build(new RecordType(record));
        BuilderWriter.writeRecord(build, annotationProcessorContext);
    }
}
