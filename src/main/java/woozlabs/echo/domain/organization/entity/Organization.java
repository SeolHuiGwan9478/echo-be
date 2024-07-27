package woozlabs.echo.domain.organization.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import woozlabs.echo.domain.member.entity.Member;
import woozlabs.echo.global.common.entity.BaseEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Organization extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private Member owner;

    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL)
    private List<MemberOrganization> memberOrganizations = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "organization_emails", joinColumns = @JoinColumn(name = "organization_id"))
    @Column(name = "email")
    private List<String> emails = new ArrayList<>();

    public void addMember(Member member) {
        MemberOrganization memberOrganization = new MemberOrganization();
        memberOrganization.setOrganization(this);
        memberOrganization.setMember(member);
        this.memberOrganizations.add(memberOrganization);
        member.getMemberOrganizations().add(memberOrganization);
    }

    public void addEmail(String email) {
        if (!this.emails.contains(email)) {
            this.emails.add(email);
        }
    }
}
