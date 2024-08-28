package woozlabs.echo.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import woozlabs.echo.domain.member.entity.Account;
import woozlabs.echo.domain.member.entity.Member;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByUid(String uid);

    Optional<Account> findByGoogleProviderId(String googleProviderId);

    Optional<Account> findByEmail(String email);

    List<Account> findAllByMember(Member member);

    @Query("SELECT m FROM Account m WHERE m.accessTokenFetchedAt <= :cutoffTime")
    List<Account> findAccountsByCutoffTime(@Param("cutoffTime") LocalDateTime cutoffTime);
}
