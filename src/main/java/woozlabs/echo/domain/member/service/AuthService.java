package woozlabs.echo.domain.member.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;
import woozlabs.echo.domain.member.entity.Account;
import woozlabs.echo.domain.member.entity.Member;
import woozlabs.echo.domain.member.entity.Role;
import woozlabs.echo.domain.member.repository.AccountRepository;
import woozlabs.echo.domain.member.repository.MemberRepository;
import woozlabs.echo.domain.member.utils.AuthCookieUtils;
import woozlabs.echo.global.constant.GlobalConstant;
import woozlabs.echo.global.exception.CustomErrorException;
import woozlabs.echo.global.exception.ErrorCode;
import woozlabs.echo.global.utils.FirebaseTokenVerifier;
import woozlabs.echo.global.utils.GoogleOAuthUtils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthService {

    private final AccountRepository accountRepository;
    private final MemberRepository memberRepository;
    private final FirebaseTokenVerifier firebaseTokenVerifier;
    private final GoogleOAuthUtils googleOAuthUtils;

    private static final String GOOGLE_PROVIDER = "google";

    private String createCustomToken(String uid) throws FirebaseAuthException {
        try {
            return FirebaseAuth.getInstance().createCustomToken(uid);
        } catch (FirebaseAuthException e) {
            throw new CustomErrorException(ErrorCode.FAILED_TO_CREATE_CUSTOM_TOKEN, e.getMessage());
        }
    }

    private void setCustomUidClaims(String uid, Map<String, Object> claims) {
        try {
            FirebaseAuth.getInstance().setCustomUserClaims(uid, claims);
        } catch (FirebaseAuthException e) {
            throw new CustomErrorException(ErrorCode.FAILED_TO_SET_CUSTOM_CLAIMS, e.getMessage());
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
            throw new CustomErrorException(ErrorCode.FAILED_TO_FETCH_GOOGLE_USER_INFO, e.getMessage());
        }
    }

    @Transactional
    public Account createOrUpdateAccount(Map<String, Object> userInfo, boolean isPrimary) {
        String providerId = (String) userInfo.get("id");
        String displayName = (String) userInfo.get("name");
        String email = (String) userInfo.get("email");
        String profileImageUrl = (String) userInfo.get("picture");
        String accessToken = (String) userInfo.get("access_token");
        String refreshToken = (String) userInfo.get("refresh_token");
        String provider = GOOGLE_PROVIDER;

        Account account = accountRepository.findByGoogleProviderId(providerId)
                .map(existingMember -> {
                    existingMember.setDisplayName(displayName);
                    existingMember.setEmail(email);
                    existingMember.setProfileImageUrl(profileImageUrl);
                    existingMember.setAccessToken(accessToken);
                    if (refreshToken != null) {
                        existingMember.setRefreshToken(refreshToken);
                    }
                    existingMember.setAccessTokenFetchedAt(LocalDateTime.now());
                    existingMember.setProvider(provider);
                    return existingMember;
                })
                .orElse(Account.builder()
                        .uid(providerId)
                        .googleProviderId(providerId)
                        .displayName(displayName)
                        .email(email)
                        .profileImageUrl(profileImageUrl)
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .accessTokenFetchedAt(LocalDateTime.now())
                        .role(Role.ROLE_USER)
                        .isPrimary(isPrimary)
                        .provider(provider)
                        .build());

        return accountRepository.save(account);
    }

    private void constructAndRedirect(HttpServletResponse response, String customToken, String displayName, String profileImageUrl, String email) {
        String url = UriComponentsBuilder.fromHttpUrl(GlobalConstant.AUTH_SIGN_IN_DOMAIN)
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
    public void handleGoogleCallback(String code, HttpServletRequest request, HttpServletResponse response) throws FirebaseAuthException {
        Map<String, Object> userInfo = getGoogleUserInfoAndTokens(code);
        String providerId = (String) userInfo.get("id");

        Optional<String> memberTokenOpt = AuthCookieUtils.getCookieValue(request);
        String memberToken = memberTokenOpt.orElse(null);

        String memberUid = null;
        if (memberToken != null) {
            memberUid = firebaseTokenVerifier.verifyTokenAndGetUid(memberToken);
        }

        if (memberUid == null) {
            log.info("Creating new Member with a new account.");
            Account account = createOrUpdateAccount(userInfo, true);

            Member member = new Member();
            member.addAccount(account);
            memberRepository.save(member);

            account.setMember(member);
            accountRepository.save(account);

            Map<String, Object> customClaims = Map.of("accounts", member.getAccounts().stream().map(Account::getUid).toList());
            setCustomUidClaims(account.getUid(), customClaims);

            log.info("Account added to Member. Account UID: {}", account.getUid());
        } else {
            log.info("Adding new account to existing Member.");
            Account existingAccount = accountRepository.findByUid(memberUid)
                    .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_SUPER_ACCOUNT));

            Member member = existingAccount.getMember();

            Account newAccount = createOrUpdateAccount(userInfo, false);
            newAccount.setMember(member);
            accountRepository.save(newAccount);

            member.addAccount(newAccount);
            memberRepository.save(member);

            Map<String, Object> customClaims = Map.of("accounts", member.getAccounts().stream().map(Account::getUid).toList());
            setCustomUidClaims(memberUid, customClaims);
        }

        constructAndRedirect(response, createCustomToken(providerId), (String) userInfo.get("name"), (String) userInfo.get("picture"), (String) userInfo.get("email"));
    }
}
