package br.dev.fabiorvs.lti.exception;

import com.nimbusds.jose.proc.BadJOSEException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(LtiValidationException.class)
    public ResponseEntity<?> handleLtiValidation(LtiValidationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                        "error", ex.getMessage()
                ));
    }

    @ExceptionHandler({BadJOSEException.class, ParseException.class})
    public ResponseEntity<?> handleJwtErrors(Exception ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                        "error", "Token LTI inválido"
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "error", "Erro interno no processamento do LTI"
                ));
    }
}