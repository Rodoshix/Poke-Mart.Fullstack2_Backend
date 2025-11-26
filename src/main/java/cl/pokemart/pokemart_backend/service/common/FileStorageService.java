package cl.pokemart.pokemart_backend.service.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private static final long MAX_SIZE_BYTES = 5 * 1024 * 1024; // 5MB

    private final Path uploadDir;

    public FileStorageService(@Value("${app.uploads.dir:uploads}") String uploadsDir) {
        this.uploadDir = Paths.get(uploadsDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadDir);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo crear el directorio de uploads: " + uploadsDir, e);
        }
    }

    public StoredFile storeImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw badRequest("El archivo de imagen es requerido");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw badRequest("Solo se aceptan imagenes JPG, PNG o WEBP");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw badRequest("La imagen supera el tama\u00f1o maximo permitido (5MB)");
        }

        String ext = resolveExtension(file.getOriginalFilename(), contentType);
        String filename = UUID.randomUUID() + (ext.isBlank() ? "" : "." + ext);

        try {
            Files.copy(file.getInputStream(), uploadDir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo guardar la imagen");
        }

        String url = "/uploads/" + filename;
        return new StoredFile(url, filename, file.getSize(), contentType);
    }

    /**
     * Borra un archivo previamente subido si corresponde al directorio configurado.
     * Retorna true si se borró, false si no aplicaba o no existía.
     */
    public boolean deleteByUrl(String imageUrl) {
        if (!StringUtils.hasText(imageUrl)) {
            return false;
        }
        try {
            String pathPart = imageUrl;
            if (imageUrl.startsWith("http")) {
                pathPart = URI.create(imageUrl).getPath();
            }
            int lastSlash = pathPart.lastIndexOf('/');
            if (lastSlash < 0 || lastSlash + 1 >= pathPart.length()) {
                return false;
            }
            String filename = pathPart.substring(lastSlash + 1);
            Path target = uploadDir.resolve(filename).normalize();
            if (!target.startsWith(uploadDir)) {
                return false;
            }
            return Files.deleteIfExists(target);
        } catch (Exception e) {
            // No propagar: si no se puede borrar, no bloquear la operación principal
            return false;
        }
    }

    private String resolveExtension(String originalFilename, String contentType) {
        String ext = "";
        if (StringUtils.hasText(originalFilename) && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf('.') + 1);
        } else if (contentType != null) {
            if (contentType.endsWith("jpeg") || contentType.endsWith("jpg")) ext = "jpg";
            else if (contentType.endsWith("png")) ext = "png";
            else if (contentType.endsWith("webp")) ext = "webp";
        }
        return ext.toLowerCase();
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    public record StoredFile(String url, String filename, long size, String contentType) {
    }
}
