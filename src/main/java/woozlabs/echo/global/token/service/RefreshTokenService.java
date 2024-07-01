package woozlabs.echo.global.token.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import woozlabs.echo.global.exception.CustomErrorException;
import woozlabs.echo.global.exception.ErrorCode;
import woozlabs.echo.global.token.entity.RefreshToken;
import woozlabs.echo.global.token.repository.RefreshTokenRepository;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    public void saveRefreshToken(Long memberId, String token) {
        RefreshToken refreshToken = new RefreshToken(memberId, token);
        refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken getAccessToken(Long memberId) {
        return refreshTokenRepository.findById(memberId)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_REFRESH_TOKEN));
    }

    public void deleteAccessToken(Long memberId) {
        refreshTokenRepository.deleteById(memberId);
    }
}
