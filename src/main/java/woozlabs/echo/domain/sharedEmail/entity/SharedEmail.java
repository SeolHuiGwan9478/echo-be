package woozlabs.echo.domain.sharedEmail.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import woozlabs.echo.domain.member.entity.Account;
import woozlabs.echo.global.common.entity.BaseEntity;

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

    @OneToOne(mappedBy = "sharedEmail", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private SharedEmailPermission sharedEmailPermission;
}
