# Record builder
Fast opinionated annotation processor to generate builders for Java records.

Supports [Gradle Incremental annotation processing](https://docs.gradle.org/current/userguide/java_plugin.html#sec:incremental_annotation_processing) leading to fewer full recompilations.


### Gradle setup
Add recordbuilder to your dependencies section in build.gradle 

```groovy
compileOnly 'com.lilbaek.recordbuilder:core:1.0.3'
annotationProcessor 'com.lilbaek.recordbuilder:processor:1.0.3'
```

### Usage
Annotate any Java record with @RecordBuilder. Compile your code and a RecordBuilder will be generated.

### Options
RecordBuilder does not expose any options (opinionated).

### Examples

Record:
```java
@RecordBuilder
public record Annotated(float start, float end, @TestAnnotation float difference, Optional<String> message) {
}
```

New instance using RecordBuilder
```java
Annotated instance = AnnotatedBuilder.builder()
        .start(0)
        .end(2)
        .difference(1)
        .message(Optional.of("message"))
        .build();
```

From existing record (Copies existing values)
```java
Annotated newInstance = AnnotatedBuilder.builder(instance)
    .message(Optional.empty())
    .build();
```

Using From to change single value
```java
Annotated newInstance = AnnotatedBuilder.from(instance)
    .withMessage(Optional.empty());
```

Default values
```java
Annotated instance = AnnotatedBuilder.builder()
        .start(2)
        .build();

assertEquals(2, instance.start());
assertEquals(0, instance.end());
assertEquals(0, instance.difference());
assertTrue(instance.message().isEmpty());
```

Annotations are automatically added to the builder
```java
public AnnotatedBuilder difference(@TestAnnotation float difference) {
        .....
}
```

The full RecordBuilder
```java
@Generated("com.lilbaek.recordbuilder.RecordBuilder")
public class AnnotatedBuilder {
    private float start;

    private float end;

    private float difference;

    private Optional<String> message = Optional.empty();

    private AnnotatedBuilder() {
    }

    private AnnotatedBuilder(float start, float end, float difference, Optional<String> message) {
        this.start = start;
        this.end = end;
        this.difference = difference;
        this.message = message;
    }

    public Annotated build() {
        return new Annotated(start, end, difference, message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end, difference, message);
    }

    @Override
    public boolean equals(Object o) {
        return (this == o) || ((o instanceof AnnotatedBuilder r)
                && (start == r.start)
                && (end == r.end)
                && (difference == r.difference)
                && Objects.equals(message, r.message));
    }

    @Override
    public String toString() {
        return "AnnotatedBuilder[start=" + start + ", end=" + end + ", difference=" + difference + ", message=" + message + "]";
    }

    public AnnotatedBuilder start(float start) {
        this.start = start;
        return this;
    }

    public AnnotatedBuilder end(float end) {
        this.end = end;
        return this;
    }

    public AnnotatedBuilder difference(@TestAnnotation float difference) {
        this.difference = difference;
        return this;
    }

    public AnnotatedBuilder message(Optional<String> message) {
        this.message = message;
        return this;
    }

    public static AnnotatedBuilder builder() {
        return new AnnotatedBuilder();
    }

    public static AnnotatedBuilder builder(Annotated from) {
        return new AnnotatedBuilder(from.start(), from.end(), from.difference(), from.message());
    }

    public static AnnotatedBuilder.With from(Annotated from) {
        return new _With(from);
    }

    @Generated("com.lilbaek.recordbuilder.RecordBuilder")
    private static final class _With implements AnnotatedBuilder.With {
        private final Annotated from;

        private _With(Annotated from) {
            this.from = from;
        }

        @Override
        public float start() {
            return from.start();
        }

        @Override
        public float end() {
            return from.end();
        }

        @Override
        public float difference() {
            return from.difference();
        }

        @Override
        public Optional<String> message() {
            return from.message();
        }
    }

    @Generated("com.lilbaek.recordbuilder.RecordBuilder")
    public interface With {
        float start();

        float end();

        @TestAnnotation
        float difference();

        Optional<String> message();

        default Annotated withStart(float start) {
            return new Annotated(start, end(), difference(), message());
        }

        default Annotated withEnd(float end) {
            return new Annotated(start(), end, difference(), message());
        }

        default Annotated withDifference(@TestAnnotation float difference) {
            return new Annotated(start(), end(), difference, message());
        }

        default Annotated withMessage(Optional<String> message) {
            return new Annotated(start(), end(), difference(), message);
        }
    }
}
```
