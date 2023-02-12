package com.lilbaek.recordbuilder.ap.internal.builder;

import com.squareup.javapoet.TypeName;

public class RecordClassType {
    public final TypeName typeName;
    public final String name;

    public RecordClassType(final TypeName typeName, final String name) {
        this.typeName = typeName;
        this.name = name;
    }
}
