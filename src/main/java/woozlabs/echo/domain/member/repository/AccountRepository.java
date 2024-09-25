package woozlabs.echo.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import woozlabs.echo.domain.member.entity.Account;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByUid(String uid);

    Optional<Account> findByProviderId(String providerId);

    Optional<Account> findByEmail(String email);

    @Query("SELECT a.id FROM Account a WHERE a.accessTokenFetchedAt < :cutoffTime")
    List<Long> findExpiredAccountIds(@Param("cutoffTime") LocalDateTime cutoffTime);

    @Query("SELECT a.uid FROM Account a " +
            "JOIN a.memberAccounts ma " +
            "GROUP BY a.id " +
            "HAVING SUM(CASE WHEN ma.member.id IN :expiredMemberIds THEN 1 ELSE 0 END) = COUNT(ma)")
    List<String> findUidsAssociatedOnlyWithExpiredMembers(@Param("expiredMemberIds") List<Long> expiredMemberIds);

    @Modifying
    @Query(value = "DELETE FROM account WHERE uid IN :uids", nativeQuery = true)
    int bulkDeleteByUids(@Param("uids") List<String> uids);
}
