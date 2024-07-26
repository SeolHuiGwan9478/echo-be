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

    Optional<Member> findByGoogleProviderId(String googleProviderId);

    Optional<Member> findByEmail(String email);

    @Query("SELECT m FROM Member m WHERE m.accessTokenFetchedAt <= :cutoffTime")
    List<Member> findMembersByCutoffTime(@Param("cutoffTime") LocalDateTime cutoffTime);
}
