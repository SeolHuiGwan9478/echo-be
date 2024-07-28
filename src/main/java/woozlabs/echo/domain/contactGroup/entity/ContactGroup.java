package woozlabs.echo.domain.contactGroup.entity;

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
public class ContactGroup extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private Member owner;

    @OneToMany(mappedBy = "contactGroup", cascade = CascadeType.ALL)
    private List<MemberContactGroup> memberContactGroups = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "contactGroup_emails", joinColumns = @JoinColumn(name = "contactGroup_id"))
    @Column(name = "email")
    private List<String> emails = new ArrayList<>();

    public void addMember(Member member) {
        MemberContactGroup memberContactGroup = new MemberContactGroup();
        memberContactGroup.setContactGroup(this);
        memberContactGroup.setMember(member);
        this.memberContactGroups.add(memberContactGroup);
        member.getMemberContactGroups().add(memberContactGroup);
    }

    public void addEmail(String email) {
        if (!this.emails.contains(email)) {
            this.emails.add(email);
        }
    }
}
