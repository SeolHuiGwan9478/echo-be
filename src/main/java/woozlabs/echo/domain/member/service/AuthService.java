package woozlabs.echo.domain.member.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import woozlabs.echo.domain.member.dto.AddAccountRequestDto;
import woozlabs.echo.domain.member.dto.SignInRequestDto;
import woozlabs.echo.domain.member.dto.GoogleResponseDto;
import woozlabs.echo.domain.member.entity.Member;
import woozlabs.echo.domain.member.entity.Role;
import woozlabs.echo.domain.member.entity.SubAccount;
import woozlabs.echo.domain.member.entity.SuperAccount;
import woozlabs.echo.domain.member.repository.MemberRepository;
import woozlabs.echo.domain.member.repository.SubAccountRepository;
import woozlabs.echo.domain.member.repository.SuperAccountRepository;
import woozlabs.echo.global.exception.CustomErrorException;
import woozlabs.echo.global.exception.ErrorCode;
import woozlabs.echo.global.token.service.AccessTokenService;
import woozlabs.echo.global.utils.FirebaseTokenVerifier;
import woozlabs.echo.global.utils.GoogleOAuthUtils;

import java.util.Map;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final SuperAccountRepository superAccountRepository;
    private final SubAccountRepository subAccountRepository;
    private final FirebaseTokenVerifier firebaseTokenVerifier;
    private final AccessTokenService accessTokenService;
    private final GoogleOAuthUtils googleOAuthUtils;

    private String createCustomToken(String uid) throws FirebaseAuthException {
        try {
            return FirebaseAuth.getInstance().createCustomToken(uid);
        } catch (FirebaseAuthException e) {
            throw new CustomErrorException(ErrorCode.FAILED_TO_CREATE_CUSTOM_TOKEN);
        }
    }

    public GoogleResponseDto signIn(String code) {
        try {
            Map<String, String> tokenResponse = googleOAuthUtils.getGoogleTokens(code);

            String accessToken = tokenResponse.get("access_token");
            Map<String, Object> userInfo = googleOAuthUtils.getGoogleUserInfo(accessToken);

            String providerId = (String) userInfo.get("id");
            String customToken = createCustomToken(providerId);

            return GoogleResponseDto.builder()
                    .providerId(providerId)
                    .displayName((String) userInfo.get("name"))
                    .email((String) userInfo.get("email"))
                    .profileImageUrl((String) userInfo.get("picture"))
                    .googleIdToken(tokenResponse.get("id_token"))
                    .customToken(customToken)
                    .googleAccessToken(accessToken)
                    .googleRefreshToken(tokenResponse.get("refresh_token"))
                    .build();
        } catch (Exception e) {
            throw new CustomErrorException(ErrorCode.FAILED_TO_FETCH_GOOGLE_USER_INFO);
        }
    }

    @Transactional
    public void createAccount(SignInRequestDto requestDto) {
        Member member = Member.builder()
                .uid(requestDto.getUid())
                .displayName(requestDto.getDisplayName())
                .email(requestDto.getEmail())
                .emailVerified(requestDto.isEmailVerified())
                .photoURL(requestDto.getPhotoURL())
                .role(Role.ROLE_USER)
                .build();

        memberRepository.save(member);

        SuperAccount superAccount = SuperAccount.builder()
                .uid(requestDto.getUid())
                .displayName(requestDto.getDisplayName())
                .email(requestDto.getEmail())
                .emailVerified(requestDto.isEmailVerified())
                .photoURL(requestDto.getPhotoURL())
                .role(Role.ROLE_USER)
                .member(member)
                .build();

        superAccountRepository.save(superAccount);

        accessTokenService.saveAccessToken(member.getId(), requestDto.getGoogleAccessToken());
    }

    @Transactional
    public void addAccount(String idToken, AddAccountRequestDto requestDto) throws FirebaseAuthException {
        String superAccountUid = firebaseTokenVerifier.verifyTokenAndGetUid(idToken);
        SuperAccount superAccount = superAccountRepository.findByUid(superAccountUid)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_SUPER_ACCOUNT));

        Member member = Member.builder()
                .uid(requestDto.getUid())
                .displayName(requestDto.getDisplayName())
                .email(requestDto.getEmail())
                .emailVerified(requestDto.isEmailVerified())
                .photoURL(requestDto.getPhotoURL())
                .role(Role.ROLE_USER)
                .build();

        memberRepository.save(member);

        SubAccount subAccount = SubAccount.builder()
                .uid(requestDto.getUid())
                .displayName(requestDto.getDisplayName())
                .email(requestDto.getEmail())
                .emailVerified(requestDto.isEmailVerified())
                .photoURL(requestDto.getPhotoURL())
                .role(Role.ROLE_USER)
                .member(member)
                .superAccount(superAccount)
                .build();

        subAccountRepository.save(subAccount);

        accessTokenService.saveAccessToken(member.getId(), requestDto.getGoogleAccessToken());
    }
}
