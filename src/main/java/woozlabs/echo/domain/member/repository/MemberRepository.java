package woozlabs.echo.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import woozlabs.echo.domain.member.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Member findByProviderAndProviderId(String provider, String providerId);
}
