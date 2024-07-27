package woozlabs.echo.domain.organization.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import woozlabs.echo.domain.member.entity.Member;
import woozlabs.echo.domain.organization.entity.Organization;

import java.util.List;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    List<Organization> findByOwner(Member owner);
}
