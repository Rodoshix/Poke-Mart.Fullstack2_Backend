package cl.pokemart.pokemart_backend.controller.common;

import cl.pokemart.pokemart_backend.dto.common.ApiErrorExamples;
import cl.pokemart.pokemart_backend.dto.common.ErrorResponse;
import cl.pokemart.pokemart_backend.service.common.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/uploads")
@Tag(name = "Uploads", description = "Carga de archivos (imágenes) para productos/usuarios")
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Solicitud inválida", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {
                @ExampleObject(name = "Sin archivo", value = """
                        {
                          "status": 400,
                          "error": "Bad Request",
                          "message": "El archivo de imagen es requerido",
                          "path": "/api/v1/uploads/images",
                          "timestamp": "2025-11-27T10:15:30Z"
                        }
                        """)
        })),
        @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {
                @ExampleObject(name = "Token faltante", value = """
                        {
                          "status": 401,
                          "error": "Unauthorized",
                          "message": "No autenticado",
                          "path": "/api/v1/uploads/images",
                          "timestamp": "2025-11-27T10:15:30Z"
                        }
                        """)
        })),
        @ApiResponse(responseCode = "403", description = "Sin permisos", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {
                @ExampleObject(name = "Rol insuficiente", value = ApiErrorExamples.OFFER_FORBIDDEN)
        })),
        @ApiResponse(responseCode = "413", description = "Archivo demasiado grande", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {
                @ExampleObject(name = "Excede tamaño", value = ApiErrorExamples.UPLOAD_TOO_LARGE)
        })),
        @ApiResponse(responseCode = "415", description = "Tipo de archivo no soportado", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {
                @ExampleObject(name = "Tipo no soportado", value = ApiErrorExamples.UPLOAD_UNSUPPORTED)
        })),
        @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {
                @ExampleObject(name = "Fallo interno", value = ApiErrorExamples.PUBLIC_OFFERS_ERROR)
        }))
})
public class UploadController {

    private final FileStorageService fileStorageService;

    public UploadController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PreAuthorize("hasAnyRole('ADMIN','VENDEDOR')")
    @Operation(summary = "Subir imagen", description = "Recibe una imagen JPG/PNG/WEBP y devuelve la URL pública.")
    @ApiResponse(responseCode = "200", description = "Imagen almacenada",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(example = "{\"url\":\"/uploads/img.png\",\"filename\":\"img.png\",\"size\":12345,\"contentType\":\"image/png\"}")))
    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> uploadImage(@RequestParam("file") MultipartFile file) {
        var stored = fileStorageService.storeImage(file);
        return Map.of(
                "url", stored.url(),
                "filename", stored.filename(),
                "size", stored.size(),
                "contentType", stored.contentType()
        );
    }
}
