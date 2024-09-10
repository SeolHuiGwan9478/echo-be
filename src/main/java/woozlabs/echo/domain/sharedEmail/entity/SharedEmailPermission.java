package woozlabs.echo.domain.sharedEmail.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SharedEmailPermission {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "shared_email_id")
    @JsonIgnore
    private SharedEmail sharedEmail;

    @ElementCollection
    @CollectionTable(name = "invitee_permissions", joinColumns = @JoinColumn(name = "shared_email_id"))
    @MapKeyColumn(name = "invitee_email")
    @Column(name = "permission")
    @Enumerated(EnumType.STRING)
    private Map<String, Permission> inviteePermissions = new HashMap<>();
}
