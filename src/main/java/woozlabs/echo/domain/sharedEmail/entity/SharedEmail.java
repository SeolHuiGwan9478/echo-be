package woozlabs.echo.domain.sharedEmail.entity;

import jakarta.persistence.*;
import lombok.*;
import woozlabs.echo.domain.member.entity.Account;
import woozlabs.echo.global.common.entity.BaseEntity;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SharedEmail extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Access access;

    private String dataId;

    @Enumerated(EnumType.STRING)
    private SharedDataType sharedDataType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private Account owner;

    private boolean canEditorEditPermission;
    private boolean canViewerViewToolMenu;

    @OneToOne(mappedBy = "sharedEmail", cascade = CascadeType.ALL, orphanRemoval = true)
    private SharedEmailPermission sharedEmailPermission;
}
