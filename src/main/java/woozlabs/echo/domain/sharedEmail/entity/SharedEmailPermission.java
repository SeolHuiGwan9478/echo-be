package woozlabs.echo.domain.sharedEmail.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

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
    private SharedEmail sharedEmail;

    @ElementCollection
    @CollectionTable(name = "invitee_permissions", joinColumns = @JoinColumn(name = "shared_email_id"))
    @MapKeyColumn(name = "invitee_email")
    @Column(name = "permission")
    @Enumerated(EnumType.STRING)
    private Map<String, Permission> inviteePermissions = new HashMap<>();
}
