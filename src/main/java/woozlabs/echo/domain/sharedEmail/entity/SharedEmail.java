package woozlabs.echo.domain.sharedEmail.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import woozlabs.echo.domain.member.entity.Account;
import woozlabs.echo.global.common.entity.BaseEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SharedEmail extends BaseEntity {

    @Id @UuidGenerator
    private UUID id;

    @Enumerated(EnumType.STRING)
    private Access access;

    private String dataId;

    @Enumerated(EnumType.STRING)
    private SharedDataType sharedDataType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    @JsonIgnore
    private Account owner;

    private boolean canEditorEditPermission;
    private boolean canViewerViewToolMenu;

    @ElementCollection
    @CollectionTable(name = "shared_email_invitee", joinColumns = @JoinColumn(name = "shared_email_id"))
    @MapKeyColumn(name = "invitee_email")
    @Column(name = "permission")
    @Enumerated(EnumType.STRING)
    private Map<String, Permission> inviteePermissions = new HashMap<>();
}
