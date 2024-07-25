package woozlabs.echo.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import woozlabs.echo.domain.member.entity.Member;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByUid(String uid);
    Optional<Member> findByEmail(String email);

    Optional<Member> findByGoogleProviderId(String googleProviderId);

    Optional<Member> findByEmail(String email);

    @Query("SELECT DISTINCT m FROM Member m " +
            "LEFT JOIN FETCH m.superAccount sa " +
            "LEFT JOIN FETCH sa.subAccounts " +
            "WHERE m.accessTokenFetchedAt <= :cutoffTime")
    List<Member> findMembersWithAccountsByCutoffTime(@Param("cutoffTime")LocalDateTime cutoffTime);
}
