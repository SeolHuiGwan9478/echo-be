package woozlabs.echo.domain.sharedEmail.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import woozlabs.echo.domain.sharedEmail.entity.Permission;
import woozlabs.echo.domain.sharedEmail.entity.SharedEmailPermission;

import java.util.Map;
import java.util.Optional;

public interface SharedEmailPermissionRepository extends JpaRepository<SharedEmailPermission, Long> {

    Optional<SharedEmailPermission> findBySharedEmailId(Long sharedEmail_id);
}
