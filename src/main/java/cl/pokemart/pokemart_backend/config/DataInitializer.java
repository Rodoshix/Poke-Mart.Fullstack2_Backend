package cl.pokemart.pokemart_backend.config;

import cl.pokemart.pokemart_backend.model.user.Role;
import cl.pokemart.pokemart_backend.service.user.UserService;
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
                    "11.111.111-1",
                    "Av. Admin 123",
                    "Metropolitana",
                    "Santiago",
                    java.time.LocalDate.of(1990, 1, 1),
                    "+56911111111",
                    Role.ADMIN
            );
            userService.ensureUser(
                    "vendedor@pokemart.cl",
                    "vendedor",
                    "Vendedor123!",
                    "Vendedor",
                    "Pokemart",
                    "22.222.222-2",
                    "Av. Vendedor 456",
                    "Metropolitana",
                    "Santiago",
                    java.time.LocalDate.of(1992, 2, 2),
                    "+56922222222",
                    Role.VENDEDOR
            );
        } catch (Exception e) {
            log.warn("No se pudo inicializar usuarios demo: {}", e.getMessage());
        }
    }
}
