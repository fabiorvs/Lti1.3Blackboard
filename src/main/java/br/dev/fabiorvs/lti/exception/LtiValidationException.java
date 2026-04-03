package br.dev.fabiorvs.lti.exception;

public class LtiValidationException extends RuntimeException {

    public LtiValidationException(String message) {
        super(message);
    }

    public LtiValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}