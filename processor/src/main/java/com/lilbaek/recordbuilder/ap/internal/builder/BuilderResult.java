package com.lilbaek.recordbuilder.ap.internal.builder;

import com.squareup.javapoet.TypeSpec;

public record BuilderResult(RecordType record, TypeSpec result) {

    public String packageName() {
        return record.packageName;
    }

    public RecordClassType builderClassType() {
        return record.builderRecordClassType;
    }
}
