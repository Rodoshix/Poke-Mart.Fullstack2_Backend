package cl.pokemart.pokemart_backend.controller.admin;

import cl.pokemart.pokemart_backend.dto.admin.AdminUserRequest;
import cl.pokemart.pokemart_backend.dto.admin.AdminUserResponse;
import cl.pokemart.pokemart_backend.dto.common.ErrorResponse;
import cl.pokemart.pokemart_backend.model.user.Role;
import cl.pokemart.pokemart_backend.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/users")
@Tag(name = "Admin - Users", description = "Gestión de usuarios (admin)")
@PreAuthorize("hasRole('ADMIN')")
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Solicitud inválida", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Sin permisos", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "No encontrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Conflicto o regla de negocio", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
})
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Listado de usuarios", description = "Devuelve el listado completo de usuarios.")
    @ApiResponse(responseCode = "200", description = "Usuarios",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AdminUserResponse.class)))
    @GetMapping
    public List<AdminUserResponse> list() {
        return userService.findAll().stream()
                .map(AdminUserResponse::from)
                .toList();
    }

    @Operation(summary = "Detalle de usuario", description = "Obtiene un usuario por ID.")
    @ApiResponse(responseCode = "200", description = "Usuario encontrado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AdminUserResponse.class)))
    @GetMapping("/{id}")
    public AdminUserResponse getOne(@PathVariable Long id) {
        return AdminUserResponse.from(userService.findById(id));
    }

    @Operation(summary = "Crear usuario", description = "Crea un usuario con rol definido.")
    @ApiResponse(responseCode = "201", description = "Usuario creado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AdminUserResponse.class)))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
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

    @Operation(summary = "Actualizar usuario", description = "Actualiza datos y rol de un usuario existente.")
    @ApiResponse(responseCode = "200", description = "Usuario actualizado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AdminUserResponse.class)))
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

    @Operation(summary = "Eliminar usuario", description = "Elimina lógicamente o físicamente un usuario.")
    @ApiResponse(responseCode = "204", description = "Eliminado")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @RequestParam(value = "hard", defaultValue = "false") boolean hard) {
        userService.deleteUser(id, hard);
    }

    @Operation(summary = "Activar/Desactivar usuario", description = "Cambia el estado activo del usuario.")
    @ApiResponse(responseCode = "200", description = "Estado actualizado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AdminUserResponse.class)))
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
