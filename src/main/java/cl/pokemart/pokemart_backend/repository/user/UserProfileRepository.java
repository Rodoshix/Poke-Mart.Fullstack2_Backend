package cl.pokemart.pokemart_backend.repository.user;

import cl.pokemart.pokemart_backend.model.user.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByUserId(Long userId);
    Optional<UserProfile> findByRut(String rut);
}
