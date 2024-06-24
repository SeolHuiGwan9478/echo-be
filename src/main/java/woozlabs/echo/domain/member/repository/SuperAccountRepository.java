package woozlabs.echo.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import woozlabs.echo.domain.member.entity.SuperAccount;

import java.util.Optional;

public interface SuperAccountRepository extends JpaRepository<SuperAccount, Long> {

    Optional<SuperAccount> findByUid(String uid);
}
