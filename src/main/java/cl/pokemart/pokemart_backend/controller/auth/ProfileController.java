package cl.pokemart.pokemart_backend.controller.auth;

import cl.pokemart.pokemart_backend.dto.auth.Profile;
import cl.pokemart.pokemart_backend.dto.auth.ProfileUpdateRequest;
import cl.pokemart.pokemart_backend.dto.common.ApiErrorExamples;
import cl.pokemart.pokemart_backend.dto.common.ErrorResponse;
import cl.pokemart.pokemart_backend.model.user.User;
import cl.pokemart.pokemart_backend.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/profile")
@Tag(name = "Perfil", description = "Gestión del perfil del usuario autenticado")
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Solicitud inválida", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {
                @ExampleObject(name = "Body invalido", value = """
                        {
                          "status": 400,
                          "error": "Bad Request",
                          "message": "El correo es obligatorio",
                          "path": "/api/v1/profile",
                          "timestamp": "2025-11-27T10:15:30Z"
                        }
                        """)
        })),
        @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {
                @ExampleObject(name = "Sin token", value = ApiErrorExamples.PROFILE_UNAUTHORIZED)
        })),
        @ApiResponse(responseCode = "403", description = "Sin permisos", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {
                @ExampleObject(name = "Rol insuficiente", value = ApiErrorExamples.OFFER_FORBIDDEN)
        })),
        @ApiResponse(responseCode = "404", description = "No encontrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {
                @ExampleObject(name = "Perfil no encontrado", value = ApiErrorExamples.USER_NOT_FOUND)
        })),
        @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {
                @ExampleObject(name = "Fallo interno", value = ApiErrorExamples.PUBLIC_OFFERS_ERROR)
        }))
})
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Perfil actual", description = "Obtiene los datos del usuario autenticado.")
    @ApiResponse(responseCode = "200", description = "Perfil",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Profile.class)))
    @GetMapping
    public Profile getProfile(@AuthenticationPrincipal User principal) {
        User user = ensureUser(principal);
        return Profile.fromUser(userService.findById(user.getId()));
    }

    @Operation(summary = "Actualizar perfil", description = "Actualiza los datos del usuario autenticado.")
    @ApiResponse(responseCode = "200", description = "Perfil actualizado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Profile.class)))
    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public Profile updateProfile(@Valid @RequestBody ProfileUpdateRequest request, Authentication authentication) {
        User user = currentUser(authentication);

        var updated = userService.updateUser(
                user.getId(),
                request.getEmail(),
                request.getUsername(),
                request.getPassword(),
                request.getNombre(),
                request.getApellido(),
                request.getRut(),
                request.getDireccion(),
                request.getRegion(),
                request.getComuna(),
                parseDate(request.getFechaNacimiento()),
                request.getTelefono(),
                user.getRole(),
                user.getActive(),
                request.getAvatarUrl()
        );

        return Profile.fromUser(updated);
    }

    private User currentUser(Authentication authentication) {
        if (authentication == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof User user) {
            return ensureUser(user);
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
    }

    private User ensureUser(User principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
        }
        return userService.findById(principal.getId());
    }

    private LocalDate parseDate(String value) {
        try {
            return value != null && !value.isBlank() ? LocalDate.parse(value) : null;
        } catch (Exception ignored) {
            return null;
        }
    }
}
