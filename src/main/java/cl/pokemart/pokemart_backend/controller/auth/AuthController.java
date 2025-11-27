package cl.pokemart.pokemart_backend.controller.auth;

import cl.pokemart.pokemart_backend.dto.auth.AuthResponse;
import cl.pokemart.pokemart_backend.dto.auth.LoginRequest;
import cl.pokemart.pokemart_backend.dto.auth.RefreshRequest;
import cl.pokemart.pokemart_backend.dto.auth.RegisterRequest;
import cl.pokemart.pokemart_backend.dto.common.ErrorResponse;
import cl.pokemart.pokemart_backend.model.user.User;
import cl.pokemart.pokemart_backend.security.JwtService;
import cl.pokemart.pokemart_backend.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "Autenticación y registro de usuarios")
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Solicitud inválida", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Credenciales inválidas / no autenticado", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Conflicto o regla de negocio", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
})
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @Operation(summary = "Iniciar sesión", description = "Autentica al usuario y devuelve tokens JWT + perfil.")
    @ApiResponse(responseCode = "200", description = "Inicio de sesión correcto",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class)))
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getIdentifier(), request.getPassword())
            );
            User user = (User) authentication.getPrincipal();
            userService.touchLastLogin(user.getId());
            return buildTokensForUser(user);
        } catch (BadCredentialsException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales invalidas");
        }
    }

    @Operation(summary = "Registrar usuario", description = "Crea cuenta de cliente y devuelve tokens JWT + perfil.")
    @ApiResponse(responseCode = "201", description = "Usuario registrado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class)))
    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.registerClient(
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
                request.getTelefono()
        );
        return buildTokensForUser(user);
    }

    @Operation(summary = "Refrescar sesión", description = "Renueva el token de acceso a partir de un refresh token válido.")
    @ApiResponse(responseCode = "200", description = "Tokens renovados",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class)))
    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest request) {
        String refresh = request.getRefreshToken();
        String type = jwtService.extractClaim(refresh, claims -> claims.get("type", String.class));
        if (!"refresh".equalsIgnoreCase(type)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token de refresh invalido");
        }
        if (jwtService.isTokenExpired(refresh)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expirado");
        }
        String username = jwtService.extractUsername(refresh);
        User user = userService.findByIdentifier(username);
        userService.touchLastLogin(user.getId());
        return buildTokensForUser(user);
    }

    private AuthResponse buildTokensForUser(User user) {
        Map<String, Object> claims = Map.of(
                "role", user.getRole().name(),
                "userId", user.getId(),
                "email", user.getEmail()
        );
        String token = jwtService.generateAccessToken(user, claims);
        String refreshToken = jwtService.generateRefreshToken(user);
        long expiresAt = jwtService.extractExpiration(token).getTime();
        return AuthResponse.of(token, expiresAt, refreshToken, user);
    }

    private LocalDate parseDate(String value) {
        try {
            return value != null && !value.isBlank() ? LocalDate.parse(value) : null;
        } catch (Exception e) {
            return null;
        }
    }
}
