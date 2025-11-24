package cl.pokemart.pokemart_backend.model.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(nullable = false, unique = true, length = 60)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private UserProfile profile;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return username != null ? username : email;
    }

    public String getDisplayName() {
        if (profile != null) {
            String fullName = (Objects.toString(profile.getNombre(), "") + " " + Objects.toString(profile.getApellido(), "")).trim();
            if (!fullName.isBlank()) {
                return fullName;
            }
        }
        return username != null ? username : email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return Boolean.TRUE.equals(active);
    }

    @Override
    public boolean isAccountNonLocked() {
        return Boolean.TRUE.equals(active);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return Boolean.TRUE.equals(active);
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(active);
    }
}
