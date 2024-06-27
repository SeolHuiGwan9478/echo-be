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

    public void saveAccessToken(String token, Long memberId) {
        AccessToken accessToken = new AccessToken(token, memberId);
        accessTokenRepository.save(accessToken);
    }

    public AccessToken getAccessToken(String token) {
        return accessTokenRepository.findById(token)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_ACCESS_TOKEN));
    }

    public void deleteAccessToken(String token) {
        accessTokenRepository.deleteById(token);
    }
}
