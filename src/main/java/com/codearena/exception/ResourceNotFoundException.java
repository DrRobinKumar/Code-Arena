package com.codearena.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException of(String resource, String field, Object value) {
        return new ResourceNotFoundException(
                "%s not found with %s = '%s'".formatted(resource, field, value));
    }
}
