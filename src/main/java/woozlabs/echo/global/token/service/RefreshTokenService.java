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

    public void saveRefreshToken(String token, Long memberId) {
        RefreshToken refreshToken = new RefreshToken(token, memberId);
        refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken getAccessToken(String token) {
        return refreshTokenRepository.findById(token)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_REFRESH_TOKEN));
    }

    public void deleteAccessToken(String token) {
        refreshTokenRepository.deleteById(token);
    }
}
