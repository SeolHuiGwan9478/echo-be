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

    private Long sharedEmailId;

    @ElementCollection
    @CollectionTable(name = "shared_email_permissions", joinColumns = @JoinColumn(name = "shared_email_id"))
    @MapKeyColumn(name = "invitee_email")
    @Column(name = "permission")
    @Enumerated(EnumType.STRING)
    private Map<String, Permission> inviteePermissions = new HashMap<>();
}
