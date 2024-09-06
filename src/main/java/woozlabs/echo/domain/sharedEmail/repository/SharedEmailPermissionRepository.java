package woozlabs.echo.domain.sharedEmail.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import woozlabs.echo.domain.sharedEmail.entity.SharedEmailPermission;

public interface SharedEmailPermissionRepository extends JpaRepository<SharedEmailPermission, Long> {
}
