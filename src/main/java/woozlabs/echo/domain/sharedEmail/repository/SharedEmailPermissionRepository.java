package woozlabs.echo.domain.sharedEmail.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import woozlabs.echo.domain.sharedEmail.entity.SharedEmailPermission;

import java.util.Optional;
import java.util.UUID;

public interface SharedEmailPermissionRepository extends JpaRepository<SharedEmailPermission, Long> {

    Optional<SharedEmailPermission> findBySharedEmailId(UUID sharedEmail_id);
}
