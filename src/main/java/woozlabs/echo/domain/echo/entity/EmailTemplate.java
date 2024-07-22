package woozlabs.echo.domain.echo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import woozlabs.echo.domain.member.entity.Member;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class EmailTemplate {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String templateName;
    private String subject;

    @ElementCollection
    private List<String> to = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String body;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;
}
