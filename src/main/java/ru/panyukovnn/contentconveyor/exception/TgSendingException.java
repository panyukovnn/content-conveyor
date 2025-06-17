package ru.panyukovnn.contentconveyor.exception;

import lombok.Getter;

@Getter
public class TgSendingException extends ConveyorException {

    public TgSendingException(String id, String message) {
        super(id, message);
    }

    public TgSendingException(String id, String message, Throwable cause) {
        super(id, message, cause);
    }
}
