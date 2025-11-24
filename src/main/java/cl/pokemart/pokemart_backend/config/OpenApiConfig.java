package cl.pokemart.pokemart_backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Pokemart API",
                version = "v1",
                description = "API REST para catálogo, pedidos y administración de Pokemart.",
                contact = @Contact(name = "Equipo Pokemart", email = "soporte@pokemart.cl"),
                license = @License(name = "MIT")
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Local"),
                @Server(url = "https://{host}", description = "Prod/OCI")
        }
)
public class OpenApiConfig {
}
