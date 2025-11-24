package cl.pokemart.pokemart_backend.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler({BadCredentialsException.class, AuthenticationCredentialsNotFoundException.class})
    public ResponseEntity<Map<String, Object>> handleAuth(Exception ex) {
        return build(HttpStatus.UNAUTHORIZED, "Credenciales invalidas");
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        return build(status != null ? status : HttpStatus.BAD_REQUEST, ex.getReason() != null ? ex.getReason() : "Error");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .orElse("Datos invalidos");
        return build(HttpStatus.BAD_REQUEST, msg);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleUploadLimit(MaxUploadSizeExceededException ex) {
        return build(HttpStatus.PAYLOAD_TOO_LARGE, "El archivo supera el limite permitido");
    }

    private ResponseEntity<Map<String, Object>> build(HttpStatus status, String message) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("status", status.value());
        payload.put("message", message);
        return ResponseEntity.status(status).body(payload);
    }
}
