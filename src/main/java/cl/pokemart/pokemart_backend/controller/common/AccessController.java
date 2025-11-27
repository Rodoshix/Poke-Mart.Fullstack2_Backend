package cl.pokemart.pokemart_backend.controller.common;

import cl.pokemart.pokemart_backend.dto.common.ApiErrorExamples;
import cl.pokemart.pokemart_backend.dto.common.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {
                @ExampleObject(name = "Sin token", value = ApiErrorExamples.PROFILE_UNAUTHORIZED)
        })),
        @ApiResponse(responseCode = "403", description = "Sin permisos", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {
                @ExampleObject(name = "Rol insuficiente", value = ApiErrorExamples.OFFER_FORBIDDEN)
        })),
        @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {
                @ExampleObject(name = "Fallo interno", value = ApiErrorExamples.PUBLIC_OFFERS_ERROR)
        }))
})
public class AccessController {

    @Operation(summary = "Ping público", description = "Endpoint público de prueba.")
    @ApiResponse(responseCode = "200", description = "OK")
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
