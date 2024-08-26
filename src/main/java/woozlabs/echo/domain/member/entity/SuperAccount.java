package woozlabs.echo.domain.member.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import woozlabs.echo.global.common.entity.BaseEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "super_account")
public class SuperAccount extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "superAccount", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Member> members = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "super_account_member_uids", joinColumns = @JoinColumn(name = "super_account_id"))
    @Column(name = "member_uid")
    private List<String> memberUids = new ArrayList<>();

    public void addMember(Member member) {
        this.members.add(member);
        if (!this.memberUids.contains(member.getUid())) {
            this.memberUids.add(member.getUid());
        }
    }
}
