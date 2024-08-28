package woozlabs.echo.domain.echo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import woozlabs.echo.domain.echo.entity.EmailTemplate;
import woozlabs.echo.domain.member.entity.Account;

import java.util.List;

public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Long> {

    List<EmailTemplate> findByAccount(Account account);
}
