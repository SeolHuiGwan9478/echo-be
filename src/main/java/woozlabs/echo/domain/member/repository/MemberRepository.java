package woozlabs.echo.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import woozlabs.echo.domain.member.entity.Member;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByGoogleProviderId(String googleProviderId);
}
