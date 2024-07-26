package woozlabs.echo.domain.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import woozlabs.echo.domain.member.entity.Member;
import woozlabs.echo.domain.member.repository.MemberRepository;
import woozlabs.echo.global.exception.CustomErrorException;
import woozlabs.echo.global.exception.ErrorCode;
import woozlabs.echo.global.utils.GoogleOAuthUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TokenSchedulerService {

    private final MemberRepository memberRepository;
    private final GoogleOAuthUtils googleOAuthUtils;

    @Scheduled(fixedRate = 5 * 60 * 1000)
    @Transactional
    public void checkAndRefreshTokens() {
        LocalDateTime cutoffTime = LocalDateTime.now().minus(57, ChronoUnit.MINUTES);
        List<Member> members = memberRepository.findMembersByCutoffTime(cutoffTime);
        for (Member member : members) {
            if (shouldRefreshToken(member.getAccessTokenFetchedAt())) {
                refreshToken(member);
            }
        }
    }

    private boolean shouldRefreshToken(LocalDateTime tokenFetchedAt) {
        if (tokenFetchedAt == null) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        long minutesElapsed = ChronoUnit.MINUTES.between(tokenFetchedAt, now);

        return minutesElapsed >= 50;
    }

    public void refreshToken(Member member) {
        try {
            Map<String, String> newTokens = googleOAuthUtils.refreshAccessToken(member.getRefreshToken());
            String newAccessToken = newTokens.get("access_token");

            member.setAccessToken(newAccessToken);
            member.setAccessTokenFetchedAt(LocalDateTime.now());
            memberRepository.save(member);
        } catch (Exception e) {
            log.error("Failed to refresh token for Member: {}", member.getId(), e);
            throw new CustomErrorException(ErrorCode.FAILED_TO_REFRESH_GOOGLE_TOKEN, "Failed to refresh token for Member: " + member.getId(), e);
        }
    }
}
