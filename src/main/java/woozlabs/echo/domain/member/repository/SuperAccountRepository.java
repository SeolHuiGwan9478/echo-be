package woozlabs.echo.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import woozlabs.echo.domain.member.entity.SuperAccount;

public interface SuperAccountRepository extends JpaRepository<SuperAccount, Long> {

    SuperAccount findByEmail(String email);
}
