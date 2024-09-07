package woozlabs.echo.domain.sharedEmail.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import woozlabs.echo.domain.sharedEmail.entity.SharedEmail;

import java.util.Optional;

public interface SharedInboxRepository extends JpaRepository<SharedEmail, Long> {

    Optional<SharedEmail> findByDataId(String dataId);

    boolean existsByDataId(String dataId);
}
