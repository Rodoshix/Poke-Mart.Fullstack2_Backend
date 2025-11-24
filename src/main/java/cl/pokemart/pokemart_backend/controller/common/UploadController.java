package cl.pokemart.pokemart_backend.controller.common;

import cl.pokemart.pokemart_backend.service.common.FileStorageService;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/uploads")
public class UploadController {

    private final FileStorageService fileStorageService;

    public UploadController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PreAuthorize("hasAnyRole('ADMIN','VENDEDOR')")
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
