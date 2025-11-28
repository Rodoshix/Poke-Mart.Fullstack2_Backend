package cl.pokemart.pokemart_backend.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
@Schema(description = "Respuesta de error estandarizada")
public class ErrorResponse {
    @Schema(description = "Codigo HTTP", example = "400")
    int status;
    @Schema(description = "Descripcion corta del error", example = "Bad Request")
    String error;
    @Schema(description = "Mensaje legible", example = "El correo es obligatorio")
    String message;
    @Schema(description = "Ruta solicitada", example = "/api/v1/products/1")
    String path;
    @Schema(description = "Instante en que ocurrio el error", example = "2025-11-27T10:15:30Z")
    Instant timestamp;
}
