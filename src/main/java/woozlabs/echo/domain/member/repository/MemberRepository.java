package woozlabs.echo.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import woozlabs.echo.domain.member.entity.Member;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    List<Member> findAllByDeletedAtBefore(LocalDateTime deletedAt);

    Optional<Member> findByPrimaryUid(String primaryUid);
}
