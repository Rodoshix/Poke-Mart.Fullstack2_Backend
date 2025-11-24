package cl.pokemart.pokemart_backend.controller.admin;

import cl.pokemart.pokemart_backend.dto.admin.AdminUserRequest;
import cl.pokemart.pokemart_backend.dto.admin.AdminUserResponse;
import cl.pokemart.pokemart_backend.model.user.Role;
import cl.pokemart.pokemart_backend.service.user.UserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PatchMapping;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<AdminUserResponse> list() {
        return userService.findAll().stream()
                .map(AdminUserResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public AdminUserResponse getOne(@PathVariable Long id) {
        return AdminUserResponse.from(userService.findById(id));
    }

    @PostMapping
    public AdminUserResponse create(@Valid @RequestBody AdminUserRequest request) {
        var role = resolveRole(request.getRole());
        var created = userService.createUser(
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
                role
        );
        if (request.getAvatarUrl() != null && !request.getAvatarUrl().isBlank()) {
            created = userService.updateUser(
                    created.getId(),
                    created.getEmail(),
                    created.getUsername(),
                    null,
                    created.getProfile() != null ? created.getProfile().getNombre() : null,
                    created.getProfile() != null ? created.getProfile().getApellido() : null,
                    created.getProfile() != null ? created.getProfile().getRut() : null,
                    created.getProfile() != null ? created.getProfile().getDireccion() : null,
                    created.getProfile() != null ? created.getProfile().getRegion() : null,
                    created.getProfile() != null ? created.getProfile().getComuna() : null,
                    created.getProfile() != null ? created.getProfile().getFechaNacimiento() : null,
                    created.getProfile() != null ? created.getProfile().getTelefono() : null,
                    created.getRole(),
                    created.getActive(),
                    request.getAvatarUrl()
            );
        }
        return AdminUserResponse.from(created);
    }

    @PutMapping("/{id}")
    public AdminUserResponse update(@PathVariable Long id, @Valid @RequestBody AdminUserRequest request) {
        var role = resolveRole(request.getRole());
        var user = userService.updateUser(
                id,
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
                role,
                request.getActive(),
                request.getAvatarUrl()
        );
        return AdminUserResponse.from(user);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, @RequestParam(value = "hard", defaultValue = "false") boolean hard) {
        userService.deleteUser(id, hard);
    }

    @PatchMapping("/{id}/status")
    public AdminUserResponse updateStatus(@PathVariable Long id, @RequestParam("active") boolean active) {
        var user = userService.setActive(id, active);
        return AdminUserResponse.from(user);
    }

    private Role resolveRole(String value) {
        if (value == null) return Role.CLIENTE;
        return switch (value.toUpperCase()) {
            case "ADMIN" -> Role.ADMIN;
            case "VENDEDOR" -> Role.VENDEDOR;
            default -> Role.CLIENTE;
        };
    }

    private java.time.LocalDate parseDate(String value) {
        try {
            return value != null && !value.isBlank() ? java.time.LocalDate.parse(value) : null;
        } catch (Exception ignored) {
            return null;
        }
    }
}
