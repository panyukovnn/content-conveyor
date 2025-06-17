package ru.panyukovnn.contentconveyor.exception;

import lombok.Getter;

@Getter
public class ConveyorException extends RuntimeException {

    private final String id;

    public ConveyorException(String id, String message) {
        super(message);
        this.id = id;
    }

    public ConveyorException(String id, String message, Throwable cause) {
        super(message, cause);
        this.id = id;
    }
}
