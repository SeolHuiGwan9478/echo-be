package woozlabs.echo.domain.contactGroup.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import woozlabs.echo.domain.member.entity.Member;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class MemberContactGroup {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contactGroup_id")
    private ContactGroup contactGroup;
}
