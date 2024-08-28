package woozlabs.echo.domain.team.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import woozlabs.echo.domain.team.entity.Team;

import java.util.List;

public interface TeamRepository extends JpaRepository<Team, Long> {

    @Query("SELECT t FROM Team t JOIN t.teamAccounts tm WHERE tm.account.uid = :accountUid")
    List<Team> findAllTeamsByAccountUid(@Param("accountUid") String accountUid);
}
