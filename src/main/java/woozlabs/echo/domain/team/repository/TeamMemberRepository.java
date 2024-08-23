package woozlabs.echo.domain.team.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import woozlabs.echo.domain.member.entity.Member;
import woozlabs.echo.domain.team.entity.TeamMember;

import java.util.List;
import java.util.Optional;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    Optional<TeamMember> findByMemberAndTeamId(Member member, Long teamId);

    List<TeamMember> findByTeamId(Long teamId);
}
