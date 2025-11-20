package cl.pokemart.pokemart_backend.controller.auth;

import cl.pokemart.pokemart_backend.dto.auth.AuthResponse;
import cl.pokemart.pokemart_backend.dto.auth.LoginRequest;
import cl.pokemart.pokemart_backend.dto.auth.RefreshRequest;
import cl.pokemart.pokemart_backend.dto.auth.RegisterRequest;
import cl.pokemart.pokemart_backend.model.user.User;
import cl.pokemart.pokemart_backend.security.JwtService;
import cl.pokemart.pokemart_backend.service.user.UserService;
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
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getIdentifier(), request.getPassword())
            );
            User user = (User) authentication.getPrincipal();
            return buildTokensForUser(user);
        } catch (BadCredentialsException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales invalidas");
        }
    }

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
