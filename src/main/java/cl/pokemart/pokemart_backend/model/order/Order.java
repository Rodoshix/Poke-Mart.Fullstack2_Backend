package cl.pokemart.pokemart_backend.model.order;

import cl.pokemart.pokemart_backend.model.user.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ordenes")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_orden", unique = true, length = 40)
    private String numeroOrden;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private User cliente;

    @Column(name = "nombre_cliente", length = 200)
    private String nombreCliente;

    @Column(name = "correo_cliente", length = 200)
    private String correoCliente;

    @Column(name = "telefono_cliente", length = 50)
    private String telefonoCliente;

    @Column(name = "direccion_envio", length = 300)
    private String direccionEnvio;

    @Column(name = "region_envio", length = 120)
    private String regionEnvio;

    @Column(name = "comuna_envio", length = 120)
    private String comunaEnvio;

    @Column(name = "referencia_envio", length = 300)
    private String referenciaEnvio;

    @Column(name = "metodo_pago", length = 60)
    private String metodoPago;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus estado;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "costo_envio", nullable = false, precision = 14, scale = 2)
    private BigDecimal costoEnvio;

    @Column(name = "descuento", nullable = false, precision = 14, scale = 2)
    private BigDecimal descuento;

    @Column(name = "impuestos", nullable = false, precision = 14, scale = 2)
    private BigDecimal impuestos;

    @Column(name = "total", nullable = false, precision = 14, scale = 2)
    private BigDecimal total;

    @Column(name = "notas", length = 1000)
    private String notas;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;

    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.creadoEn = now;
        this.actualizadoEn = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.actualizadoEn = LocalDateTime.now();
    }
}
