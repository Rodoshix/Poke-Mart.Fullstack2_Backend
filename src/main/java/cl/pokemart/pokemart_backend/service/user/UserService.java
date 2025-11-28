package cl.pokemart.pokemart_backend.service.user;

import cl.pokemart.pokemart_backend.model.user.Role;
import cl.pokemart.pokemart_backend.model.user.User;
import cl.pokemart.pokemart_backend.model.user.UserProfile;
import cl.pokemart.pokemart_backend.repository.order.OrderRepository;
import cl.pokemart.pokemart_backend.repository.user.UserProfileRepository;
import cl.pokemart.pokemart_backend.repository.user.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Transactional
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final OrderRepository orderRepository;
    private static final long CACHE_TTL_MS = 60_000; // 60s
    private final ConcurrentHashMap<String, CacheEntry<List<User>>> adminUsersCache = new ConcurrentHashMap<>();

    public UserService(UserRepository userRepository,
                       UserProfileRepository userProfileRepository,
                       PasswordEncoder passwordEncoder,
                       OrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.orderRepository = orderRepository;
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

    @Transactional(readOnly = true)
    public java.util.List<User> findAll() {
        List<User> cached = getAdminUsersCache();
        if (cached != null) return cached;
        List<User> users = userRepository.findAllWithProfile();
        putAdminUsersCache(users);
        return users;
    }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }

    public User setActive(Long id, boolean active) {
        User user = findById(id);
        user.setActive(active);
        return userRepository.save(user);
    }

    public void deleteUser(Long id, boolean hardDelete) {
        if (!hardDelete) {
            setActive(id, false);
            return;
        }
        User user = findById(id);
        if (user.getRole() == Role.ADMIN) {
            throw new AccessDeniedException("No se puede eliminar un administrador activo");
        }

        var orders = orderRepository.findByCliente(user);
        if (orders != null && !orders.isEmpty()) {
            orders.forEach(o -> o.setCliente(null));
            orderRepository.saveAll(orders);
        }

        userRepository.delete(user);
    }

    public User updateUser(Long id,
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
                           String telefono,
                           Role role,
                           Boolean active,
                           String avatarUrl) {
        User user = userRepository.findById(id).orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        String normalizedEmail = email != null ? normalize(email) : user.getEmail();
        String normalizedUsername = username != null ? username.trim() : user.getUsername();

        if (!StringUtils.hasText(normalizedEmail)) {
            throw new IllegalArgumentException("Email requerido");
        }
        if (!StringUtils.hasText(normalizedUsername)) {
            throw new IllegalArgumentException("Username requerido");
        }
        userRepository.findByEmailIgnoreCase(normalizedEmail)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> { throw new IllegalArgumentException("Email ya registrado"); });
        userRepository.findByUsernameIgnoreCase(normalizedUsername)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> { throw new IllegalArgumentException("Username ya registrado"); });

        user.setEmail(normalizedEmail);
        user.setUsername(normalizedUsername);
        if (StringUtils.hasText(rawPassword)) {
            user.setPassword(passwordEncoder.encode(rawPassword));
        }
        if (role != null) {
            user.setRole(role);
        }
        if (active != null) {
            user.setActive(active);
        }
        user.setAvatarUrl(avatarUrl);

        UserProfile profile = user.getProfile() != null ? user.getProfile() : new UserProfile();
        profile.setUser(user);

        if (rut != null && userProfileRepository.findByRut(rut.trim()).filter(p -> !p.getUser().getId().equals(id)).isPresent()) {
            throw new IllegalArgumentException("RUT ya registrado");
        }

        if (firstName != null) profile.setNombre(firstName);
        if (lastName != null) profile.setApellido(lastName);
        if (rut != null) profile.setRut(rut.trim());
        if (direccion != null) profile.setDireccion(direccion);
        if (region != null) profile.setRegion(region);
        if (comuna != null) profile.setComuna(comuna);
        if (fechaNacimiento != null) profile.setFechaNacimiento(fechaNacimiento);
        if (telefono != null) profile.setTelefono(telefono);

        user.setProfile(profile);
        return userRepository.save(user);
    }

    public void touchLastLogin(Long userId) {
        if (userId == null) return;
        userRepository.findById(userId).ifPresent(user -> {
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);
        });
    }

    private List<User> getAdminUsersCache() {
        CacheEntry<List<User>> entry = adminUsersCache.get("all");
        if (entry == null) return null;
        if (entry.expiresAt < System.currentTimeMillis()) {
            adminUsersCache.remove("all");
            return null;
        }
        return entry.value;
    }

    private void putAdminUsersCache(List<User> users) {
        adminUsersCache.put("all", new CacheEntry<>(users, System.currentTimeMillis() + CACHE_TTL_MS));
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().toLowerCase();
    }

    private record CacheEntry<T>(T value, long expiresAt) {}
}
