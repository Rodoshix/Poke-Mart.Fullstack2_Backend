package cl.pokemart.pokemart_backend.controller.common;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/access")
@Tag(name = "Access", description = "Pings de acceso público/rol para probar autenticación")
public class AccessController {

    @Operation(summary = "Ping público", description = "Endpoint público de prueba.")
    @GetMapping("/public")
    public Map<String, Object> publicPing() {
        return Map.of("status", "ok", "scope", "public");
    }

    @PreAuthorize("hasAnyRole('CLIENTE','ADMIN','VENDEDOR')")
    @Operation(summary = "Ping usuario autenticado", description = "Devuelve info básica del usuario logueado.")
    @GetMapping("/user")
    public Map<String, Object> userPing(Authentication auth) {
        return Map.of(
                "status", "ok",
                "scope", "user",
                "name", auth != null ? auth.getName() : null,
                "roles", auth != null ? auth.getAuthorities() : null
        );
    }

    @PreAuthorize("hasAnyRole('VENDEDOR','ADMIN')")
    @Operation(summary = "Ping vendedor", description = "Solo vendedores/admin.")
    @GetMapping("/seller")
    public Map<String, Object> sellerPing(Authentication auth) {
        return Map.of(
                "status", "ok",
                "scope", "seller",
                "name", auth != null ? auth.getName() : null
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Ping admin", description = "Solo administradores.")
    @GetMapping("/admin")
    public Map<String, Object> adminPing(Authentication auth) {
        return Map.of(
                "status", "ok",
                "scope", "admin",
                "name", auth != null ? auth.getName() : null
        );
    }
}
