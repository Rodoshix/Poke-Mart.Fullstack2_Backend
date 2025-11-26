package cl.pokemart.pokemart_backend.controller.auth;

import cl.pokemart.pokemart_backend.dto.auth.Profile;
import cl.pokemart.pokemart_backend.dto.auth.ProfileUpdateRequest;
import cl.pokemart.pokemart_backend.model.user.User;
import cl.pokemart.pokemart_backend.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/profile")
@Tag(name = "Perfil", description = "Gestión del perfil del usuario autenticado")
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Perfil actual", description = "Obtiene los datos del usuario autenticado.")
    @GetMapping
    public Profile getProfile(@AuthenticationPrincipal User principal) {
        User user = ensureUser(principal);
        return Profile.fromUser(userService.findById(user.getId()));
    }

    @Operation(summary = "Actualizar perfil", description = "Actualiza los datos del usuario autenticado.")
    @PutMapping
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
