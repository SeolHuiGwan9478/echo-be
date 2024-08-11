package woozlabs.echo.domain.signature;

import jakarta.persistence.*;
import lombok.Getter;
import woozlabs.echo.global.common.entity.BaseEntity;

@Entity
@Getter
public class Signature extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    private String content;

    private String name;

    @Enumerated(EnumType.STRING)
    private SignatureType type;

    @Column(name = "owner_id")
    private Long ownerId; // teamId or memberId

    public enum SignatureType {
        MEMBER, TEAM
    }
}
