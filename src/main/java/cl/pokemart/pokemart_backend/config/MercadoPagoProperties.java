package cl.pokemart.pokemart_backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "mercadopago")
public class MercadoPagoProperties {
    /**
     * Access token de sandbox (completar con tu valor).
     */
    private String accessToken;
    /**
     * Integrator ID de sandbox (opcional).
     */
    private String integratorId;
    /**
     * URL pública para recibir webhooks de Mercado Pago.
     */
    private String notificationUrl;
    /**
     * URL de éxito a la que Mercado Pago redirige al usuario.
     */
    private String successUrl;
    /**
     * URL de fallo a la que Mercado Pago redirige al usuario.
     */
    private String failureUrl;
    /**
     * URL de pendiente a la que Mercado Pago redirige al usuario.
     */
    private String pendingUrl;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getIntegratorId() {
        return integratorId;
    }

    public void setIntegratorId(String integratorId) {
        this.integratorId = integratorId;
    }

    public String getNotificationUrl() {
        return notificationUrl;
    }

    public void setNotificationUrl(String notificationUrl) {
        this.notificationUrl = notificationUrl;
    }

    public String getSuccessUrl() {
        return successUrl;
    }

    public void setSuccessUrl(String successUrl) {
        this.successUrl = successUrl;
    }

    public String getFailureUrl() {
        return failureUrl;
    }

    public void setFailureUrl(String failureUrl) {
        this.failureUrl = failureUrl;
    }

    public String getPendingUrl() {
        return pendingUrl;
    }

    public void setPendingUrl(String pendingUrl) {
        this.pendingUrl = pendingUrl;
    }
}
