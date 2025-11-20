package cl.pokemart.pokemart_backend.service.user;

import cl.pokemart.pokemart_backend.model.user.Role;
import cl.pokemart.pokemart_backend.model.user.User;
import cl.pokemart.pokemart_backend.model.user.UserProfile;
import cl.pokemart.pokemart_backend.repository.user.UserProfileRepository;
import cl.pokemart.pokemart_backend.repository.user.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;

@Service
@Transactional
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, UserProfileRepository userProfileRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        return findByIdentifier(identifier);
    }

    public User findByIdentifier(String identifier) {
        String normalized = normalize(identifier);
        return userRepository.findByEmailIgnoreCase(normalized)
                .or(() -> userRepository.findByUsernameIgnoreCase(normalized))
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + identifier));
    }

    public User registerClient(
            String email,
            String username,
            String rawPassword,
            String firstName,
            String lastName,
            String rut,
            String direccion,
            String region,
            String comuna,
            LocalDate fechaNacimiento,
            String telefono
    ) {
        return createUser(email, username, rawPassword, firstName, lastName, rut, direccion, region, comuna, fechaNacimiento, telefono, Role.CLIENTE);
    }

    public User createUser(String email,
                           String username,
                           String rawPassword,
                           String firstName,
                           String lastName,
                           String rut,
                           String direccion,
                           String region,
                           String comuna,
                           LocalDate fechaNacimiento,
                           String telefono,
                           Role role) {
        String normalizedEmail = normalize(email);
        String normalizedUsername = username != null ? username.trim() : null;

        if (!StringUtils.hasText(normalizedEmail)) {
            throw new IllegalArgumentException("Email requerido");
        }
        if (!StringUtils.hasText(normalizedUsername)) {
            throw new IllegalArgumentException("Username requerido");
        }
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new IllegalArgumentException("Email ya registrado");
        }
        if (userRepository.existsByUsernameIgnoreCase(normalizedUsername)) {
            throw new IllegalArgumentException("Username ya registrado");
        }
        if (!StringUtils.hasText(rawPassword)) {
            throw new IllegalArgumentException("Password requerido");
        }

        User user = User.builder()
                .email(normalizedEmail)
                .username(normalizedUsername)
                .password(passwordEncoder.encode(rawPassword))
                .role(role)
                .active(true)
                .build();
        if (rut != null && userProfileRepository.findByRut(rut.trim()).isPresent()) {
            throw new IllegalArgumentException("RUT ya registrado");
        }

        UserProfile profile = UserProfile.builder()
                .user(user)
                .nombre(firstName)
                .apellido(lastName)
                .rut(rut != null ? rut.trim() : null)
                .direccion(direccion)
                .region(region)
                .comuna(comuna)
                .fechaNacimiento(fechaNacimiento)
                .telefono(telefono)
                .build();

        user.setProfile(profile);
        return userRepository.save(user);
    }

    public User ensureUser(String email,
                           String username,
                           String rawPassword,
                           String firstName,
                           String lastName,
                           String rut,
                           String direccion,
                           String region,
                           String comuna,
                           LocalDate fechaNacimiento,
                           String telefono,
                           Role role) {
        String normalizedEmail = normalize(email);
        String trimmedUsername = username != null ? username.trim() : null;
        return userRepository.findByEmailIgnoreCase(normalizedEmail)
                .or(() -> trimmedUsername == null ? java.util.Optional.empty() : userRepository.findByUsernameIgnoreCase(trimmedUsername))
                .orElseGet(() -> createUser(email, username, rawPassword, firstName, lastName, rut, direccion, region, comuna, fechaNacimiento, telefono, role));
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().toLowerCase();
    }
}
