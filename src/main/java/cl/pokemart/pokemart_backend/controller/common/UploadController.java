package cl.pokemart.pokemart_backend.controller.common;

import cl.pokemart.pokemart_backend.service.common.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
