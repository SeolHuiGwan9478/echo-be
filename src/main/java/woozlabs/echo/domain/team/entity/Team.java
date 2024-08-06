package woozlabs.echo.domain.team.entity;

import jakarta.persistence.*;
import lombok.Getter;
import woozlabs.echo.domain.member.entity.Member;
import woozlabs.echo.domain.signature.Signature;
import woozlabs.echo.global.common.entity.BaseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Getter
public class Team extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member creator;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL)
    private List<TeamMember> teamMembers = new ArrayList<>();

    @OneToMany(mappedBy = "ownerId", cascade = CascadeType.ALL)
    private List<Signature> allSignatures = new ArrayList<>();

    public List<Signature> getSignatureType() {
        return allSignatures.stream()
                .filter(signature -> signature.getType() == Signature.SignatureType.TEAM)
                .collect(Collectors.toList());
    }
}
