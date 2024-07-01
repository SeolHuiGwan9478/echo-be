package woozlabs.echo.global.token.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import woozlabs.echo.global.token.repository.AccessTokenRepository;
import woozlabs.echo.global.token.entity.AccessToken;
import woozlabs.echo.global.exception.CustomErrorException;
import woozlabs.echo.global.exception.ErrorCode;

@Service
@RequiredArgsConstructor
public class AccessTokenService {

    private final AccessTokenRepository accessTokenRepository;

    public void saveAccessToken(Long memberId, String token) {
        AccessToken accessToken = new AccessToken(memberId, token);
        accessTokenRepository.save(accessToken);
    }

    public AccessToken getAccessToken(Long memberId) {
        return accessTokenRepository.findById(memberId)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_ACCESS_TOKEN));
    }

    public void deleteAccessToken(Long memberId) {
        accessTokenRepository.deleteById(memberId);
    }
}
