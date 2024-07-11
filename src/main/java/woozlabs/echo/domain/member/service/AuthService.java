package woozlabs.echo.domain.member.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;
import woozlabs.echo.domain.member.entity.Member;
import woozlabs.echo.domain.member.entity.Role;
import woozlabs.echo.domain.member.entity.SubAccount;
import woozlabs.echo.domain.member.entity.SuperAccount;
import woozlabs.echo.domain.member.repository.MemberRepository;
import woozlabs.echo.domain.member.repository.SubAccountRepository;
import woozlabs.echo.domain.member.repository.SuperAccountRepository;
import woozlabs.echo.global.exception.CustomErrorException;
import woozlabs.echo.global.exception.ErrorCode;
import woozlabs.echo.global.utils.FirebaseTokenVerifier;
import woozlabs.echo.global.utils.GoogleOAuthUtils;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final SuperAccountRepository superAccountRepository;
    private final SubAccountRepository subAccountRepository;
    private final FirebaseTokenVerifier firebaseTokenVerifier;
    private final GoogleOAuthUtils googleOAuthUtils;

    private String createCustomToken(String uid) throws FirebaseAuthException {
        try {
            return FirebaseAuth.getInstance().createCustomToken(uid);
        } catch (FirebaseAuthException e) {
            throw new CustomErrorException(ErrorCode.FAILED_TO_CREATE_CUSTOM_TOKEN);
        }
    }

    private Map<String, Object> getGoogleUserInfoAndTokens(String code) {
        try {
            // Google Token 추출
            Map<String, String> tokenResponse = googleOAuthUtils.getGoogleTokens(code);
            String accessToken = tokenResponse.get("access_token");
            String refreshToken = tokenResponse.get("refresh_token");

            // userInfo에 Token 삽입해서 반환
            Map<String, Object> userInfo = googleOAuthUtils.getGoogleUserInfo(accessToken);
            userInfo.put("access_token", accessToken);
            userInfo.put("refresh_token", refreshToken);

            return userInfo;
        } catch (Exception e) {
            throw new CustomErrorException(ErrorCode.FAILED_TO_FETCH_GOOGLE_USER_INFO);
        }
    }

    private Member createOrUpdateMember(Map<String, Object> userInfo) {
        String providerId = (String) userInfo.get("id");
        String displayName = (String) userInfo.get("name");
        String email = (String) userInfo.get("email");
        String profileImageUrl = (String) userInfo.get("picture");
        String accessToken = (String) userInfo.get("access_token");
        String refreshToken = (String) userInfo.get("refresh_token");

        Member member = memberRepository.findByGoogleProviderId(providerId)
                .map(existingMember -> {
                    existingMember.setDisplayName(displayName);
                    existingMember.setEmail(email);
                    existingMember.setProfileImageUrl(profileImageUrl);
                    existingMember.setAccessToken(accessToken);
                    if (refreshToken != null) {
                        existingMember.setRefreshToken(refreshToken);
                    }
                    existingMember.setAccessTokenFetchedAt(LocalDateTime.now());
                    return existingMember;
                })
                .orElse(Member.builder()
                        .uid(providerId)
                        .googleProviderId(providerId)
                        .displayName(displayName)
                        .email(email)
                        .profileImageUrl(profileImageUrl)
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .accessTokenFetchedAt(LocalDateTime.now())
                        .role(Role.ROLE_USER)
                        .build());

        return memberRepository.save(member);
    }

    private void constructAndRedirect(HttpServletResponse response, String customToken, String displayName, String profileImageUrl, String email) {
        String url = UriComponentsBuilder.fromHttpUrl("https://echo-homepage.vercel.app/sign-in")
                .queryParam("customToken", customToken)
                .queryParam("displayName", displayName)
                .queryParam("profileImageUrl", profileImageUrl)
                .queryParam("email", email)
                .toUriString();

        try {
            response.sendRedirect(url);
        } catch (Exception e) {
            throw new CustomErrorException(ErrorCode.FAILED_TO_REDIRECT_GOOGLE_USER_INFO);
        }
    }

    @Transactional
    public void signIn(String code, HttpServletResponse response) throws FirebaseAuthException {
        Map<String, Object> userInfo = getGoogleUserInfoAndTokens(code);
        String providerId = (String) userInfo.get("id");
        String customToken = createCustomToken(providerId);

        Member member = createOrUpdateMember(userInfo);

        SuperAccount superAccount = superAccountRepository.findByMember(member)
                .orElse(SuperAccount.builder()
                        .uid(providerId)
                        .googleProviderId(providerId)
                        .displayName(member.getDisplayName())
                        .email(member.getEmail())
                        .profileImageUrl(member.getProfileImageUrl())
                        .accessToken(member.getAccessToken())
                        .refreshToken(member.getRefreshToken())
                        .accessTokenFetchedAt(LocalDateTime.now())
                        .role(Role.ROLE_USER)
                        .member(member)
                        .build());

        superAccountRepository.save(superAccount);

        constructAndRedirect(response, customToken, member.getDisplayName(), member.getProfileImageUrl(), member.getEmail());
    }

    @Transactional
    public void addAccount(String idToken, String code, HttpServletResponse response) throws FirebaseAuthException {
        String superAccountUid = firebaseTokenVerifier.verifyTokenAndGetUid(idToken);
        SuperAccount superAccount = superAccountRepository.findByUid(superAccountUid)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_SUPER_ACCOUNT));

        Map<String, Object> userInfo = getGoogleUserInfoAndTokens(code);
        String providerId = (String) userInfo.get("id");
        String customToken = createCustomToken(providerId);

        Member member = createOrUpdateMember(userInfo);

        SubAccount subAccount = SubAccount.builder()
                .uid(providerId)
                .googleProviderId(providerId)
                .displayName(member.getDisplayName())
                .email(member.getEmail())
                .profileImageUrl(member.getProfileImageUrl())
                .accessToken(member.getAccessToken())
                .refreshToken(member.getRefreshToken())
                .accessTokenFetchedAt(LocalDateTime.now())
                .role(Role.ROLE_USER)
                .member(member)
                .superAccount(superAccount)
                .build();

        subAccountRepository.save(subAccount);

        constructAndRedirect(response, customToken, member.getDisplayName(), member.getProfileImageUrl(), member.getEmail());
    }
}
