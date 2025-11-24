package cl.pokemart.pokemart_backend.controller.common;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/access")
public class AccessController {

    @GetMapping("/public")
    public Map<String, Object> publicPing() {
        return Map.of("status", "ok", "scope", "public");
    }

    @PreAuthorize("hasAnyRole('CLIENTE','ADMIN','VENDEDOR')")
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
    @GetMapping("/seller")
    public Map<String, Object> sellerPing(Authentication auth) {
        return Map.of(
                "status", "ok",
                "scope", "seller",
                "name", auth != null ? auth.getName() : null
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public Map<String, Object> adminPing(Authentication auth) {
        return Map.of(
                "status", "ok",
                "scope", "admin",
                "name", auth != null ? auth.getName() : null
        );
    }
}
