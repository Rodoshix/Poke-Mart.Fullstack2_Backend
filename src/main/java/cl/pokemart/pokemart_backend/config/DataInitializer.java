package cl.pokemart.pokemart_backend.config;

import cl.pokemart.pokemart_backend.user.Role;
import cl.pokemart.pokemart_backend.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserService userService;

    public DataInitializer(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(String... args) {
        try {
            userService.ensureUser(
                    "admin@pokemart.cl",
                    "admin",
                    "Admin123!",
                    "Admin",
                    "Pokemart",
                    Role.ADMIN
            );
            userService.ensureUser(
                    "vendedor@pokemart.cl",
                    "vendedor",
                    "Vendedor123!",
                    "Vendedor",
                    "Pokemart",
                    Role.VENDEDOR
            );
        } catch (Exception e) {
            log.warn("No se pudo inicializar usuarios demo: {}", e.getMessage());
        }
    }
}
