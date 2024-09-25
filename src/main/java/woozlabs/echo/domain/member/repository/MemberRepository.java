package woozlabs.echo.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import woozlabs.echo.domain.member.entity.Member;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    @Query("SELECT m.id FROM Member m WHERE m.deletedAt < :date")
    List<Long> findExpiredMemberIds(@Param("date") LocalDateTime date);

    Optional<Member> findByPrimaryUid(String primaryUid);

    @Modifying
    @Query(value = "DELETE FROM member WHERE id IN :ids", nativeQuery = true)
    int bulkDeleteByIds(@Param("ids") List<Long> ids);
}
