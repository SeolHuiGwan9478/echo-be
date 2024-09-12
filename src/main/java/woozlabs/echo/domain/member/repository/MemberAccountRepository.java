package woozlabs.echo.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import woozlabs.echo.domain.member.entity.Account;
import woozlabs.echo.domain.member.entity.Member;
import woozlabs.echo.domain.member.entity.MemberAccount;

import java.util.Optional;

public interface MemberAccountRepository extends JpaRepository<MemberAccount, Long> {

    Optional<MemberAccount> findByMemberAndAccount(Member member, Account account);
}
