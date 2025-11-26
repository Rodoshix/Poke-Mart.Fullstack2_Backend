package cl.pokemart.pokemart_backend.repository.payment;

import cl.pokemart.pokemart_backend.model.payment.PaymentIntent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentIntentRepository extends JpaRepository<PaymentIntent, Long> {
    Optional<PaymentIntent> findByExternalReference(String externalReference);
    Optional<PaymentIntent> findByPreferenceId(String preferenceId);
}
