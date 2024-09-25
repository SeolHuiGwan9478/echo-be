package woozlabs.echo.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import woozlabs.echo.domain.member.entity.Account;
import woozlabs.echo.domain.member.entity.Member;
import woozlabs.echo.domain.member.entity.MemberAccount;

import java.util.List;
import java.util.Optional;

public interface MemberAccountRepository extends JpaRepository<MemberAccount, Long> {

    Optional<MemberAccount> findByMemberAndAccount(Member member, Account account);

    List<MemberAccount> findByAccount(Account account);

    @Query("SELECT ma.account FROM MemberAccount ma WHERE ma.member = :member")
    List<Account> findAllAccountsByMember(@Param("member") Member member);

    @Modifying
    @Query(value = "DELETE FROM member_account WHERE member_id IN :memberIds", nativeQuery = true)
    int bulkDeleteByMemberIds(@Param("memberIds") List<Long> memberIds);
}
