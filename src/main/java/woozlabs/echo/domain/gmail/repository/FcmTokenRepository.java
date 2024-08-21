package woozlabs.echo.domain.gmail.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import woozlabs.echo.domain.gmail.entity.FcmToken;
import woozlabs.echo.domain.member.entity.Member;

import java.util.List;
import java.util.Optional;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {
    List<FcmToken> findByMember(Member member);
    Optional<FcmToken> findByMemberAndMachineUuid(Member member, String machineUuid);

    @Query("select count(fcm) from FcmToken fcm where fcm.member = :member")
    Long findTokenCountByMember(@Param("member") Member member);
}