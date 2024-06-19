package woozlabs.echo.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import woozlabs.echo.domain.member.entity.SubAccount;

public interface SubAccountRepository extends JpaRepository<SubAccount, Long> {
}
