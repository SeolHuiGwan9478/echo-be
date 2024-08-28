package woozlabs.echo.domain.team.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import woozlabs.echo.domain.member.entity.Account;
import woozlabs.echo.domain.team.entity.TeamAccount;

import java.util.List;
import java.util.Optional;

public interface TeamMemberRepository extends JpaRepository<TeamAccount, Long> {

    Optional<TeamAccount> findByAccountAndTeamId(Account account, Long teamId);

    List<TeamAccount> findByTeamId(Long teamId);
}
