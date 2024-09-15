package woozlabs.echo.domain.member.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;
import woozlabs.echo.domain.member.entity.Account;
import woozlabs.echo.domain.member.entity.Member;
import woozlabs.echo.domain.member.entity.MemberAccount;
import woozlabs.echo.domain.member.entity.Role;
import woozlabs.echo.domain.member.repository.AccountRepository;
import woozlabs.echo.domain.member.repository.MemberAccountRepository;
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
    private final MemberAccountRepository memberAccountRepository;
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

    private void constructAndRedirect(HttpServletResponse response, String customToken, String displayName, String profileImageUrl, String email, boolean isAddAccount) {
        String url;

        if (isAddAccount) {
            url = UriComponentsBuilder.fromHttpUrl(GlobalConstant.AUTH_ADD_ACCOUNT_DOMAIN)
                    .queryParam("customToken", customToken)
                    .queryParam("displayName", displayName)
                    .queryParam("profileImageUrl", profileImageUrl)
                    .queryParam("email", email)
                    .toUriString();
        } else {
            url = UriComponentsBuilder.fromHttpUrl(GlobalConstant.AUTH_SIGN_IN_DOMAIN)
                    .queryParam("customToken", customToken)
                    .queryParam("displayName", displayName)
                    .queryParam("profileImageUrl", profileImageUrl)
                    .queryParam("email", email)
                    .toUriString();
        }

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

        Optional<Account> existingAccountOpt = accountRepository.findByGoogleProviderId(providerId);

        if (existingAccountOpt.isPresent()) {
            Account existingAccount = existingAccountOpt.get();
            handleExistingAccount(existingAccount, userInfo, request, response);
        } else {
            handleNewAccount(userInfo, request, response);
        }
    }

    @Transactional
    public void handleExistingAccount(Account existingAccount, Map<String, Object> userInfo, HttpServletRequest request, HttpServletResponse response) throws FirebaseAuthException {
        updateAccountInfo(existingAccount, userInfo);

        Optional<String> cookieTokenOpt = AuthCookieUtils.getCookieValue(request);

        if (cookieTokenOpt.isPresent()) {
            String uid = firebaseTokenVerifier.verifyTokenAndGetUid(cookieTokenOpt.get());
            Member cookieTokenMember = memberRepository.findByPrimaryUid(uid)
                    .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_MEMBER));

            addNewAccountToExistingMember(cookieTokenMember, userInfo, response);
        } else {
            accountRepository.save(existingAccount);

            log.info("Updated existing Account. Account UID: {}", existingAccount.getUid());

            constructAndRedirect(response, createCustomToken(existingAccount.getUid()), (String) userInfo.get("name"), (String) userInfo.get("picture"), (String) userInfo.get("email"), false);
        }
    }

    public void handleNewAccount(Map<String, Object> userInfo, HttpServletRequest request, HttpServletResponse response) throws FirebaseAuthException {
        Optional<String> cookieTokenOpt = AuthCookieUtils.getCookieValue(request);

        if (cookieTokenOpt.isPresent()) {
            String tokenValue = cookieTokenOpt.get();
            log.info("Token found in cookie: {}", tokenValue);

            String uid = firebaseTokenVerifier.verifyTokenAndGetUid(tokenValue);
            log.info("Token verified successfully, UID: {}", uid);

            Member cookieTokenMember = memberRepository.findByPrimaryUid(uid)
                    .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_MEMBER));

            if (cookieTokenMember != null) {
                log.info("Member found with UID: {}, adding new account.", uid);
                addNewAccountToExistingMember(cookieTokenMember, userInfo, response);
            } else {
                log.warn("Unexpected case: Member is null after lookup.");
                createNewMemberWithAccount(userInfo, response);
            }
        } else {
            log.info("No token found in cookie, creating new member with account.");
            createNewMemberWithAccount(userInfo, response);
        }
    }

    @Transactional
    public void addNewAccountToExistingMember(Member member, Map<String, Object> userInfo, HttpServletResponse response) throws FirebaseAuthException {
        Account newAccount = createOrUpdateAccount(userInfo, false);
        MemberAccount memberAccount = new MemberAccount(member, newAccount);

        member.addMemberAccount(memberAccount);

        Map<String, Object> customClaims = Map.of("primaryMemberUid", member.getPrimaryUid());
        setCustomUidClaims(newAccount.getUid(), customClaims);

        memberRepository.save(member);
        accountRepository.save(newAccount);
        memberAccountRepository.save(memberAccount);

        log.info("Added new account to existing Member. Account UID: {}", newAccount.getUid());

        constructAndRedirect(response, createCustomToken(newAccount.getUid()), (String) userInfo.get("name"), (String) userInfo.get("picture"), (String) userInfo.get("email"), true);
    }

    @Transactional
    public void createNewMemberWithAccount(Map<String, Object> userInfo, HttpServletResponse response) throws FirebaseAuthException {
        Account account = createOrUpdateAccount(userInfo, true);

        Member member = new Member();
        member.setPrimaryUid(account.getUid());

        MemberAccount memberAccount = new MemberAccount(member, account);
        member.addMemberAccount(memberAccount);
        account.getMemberAccounts().add(memberAccount);

        Map<String, Object> customClaims = Map.of("primaryMemberUid", member.getPrimaryUid());
        setCustomUidClaims(account.getUid(), customClaims);

        memberRepository.save(member);
        accountRepository.save(account);

        log.info("Created new Member with a new account. Account UID: {}", account.getUid());

        String customToken = createCustomToken(account.getUid());
        constructAndRedirect(response, customToken, (String) userInfo.get("name"), (String) userInfo.get("picture"), (String) userInfo.get("email"), false);
    }

    private void updateAccountInfo(Account account, Map<String, Object> userInfo) {
        account.setDisplayName((String) userInfo.get("name"));
        account.setEmail((String) userInfo.get("email"));
        account.setProfileImageUrl((String) userInfo.get("picture"));
        account.setAccessToken((String) userInfo.get("access_token"));
        String refreshToken = (String) userInfo.get("refresh_token");
        if (refreshToken != null) {
            account.setRefreshToken(refreshToken);
        }
        account.setAccessTokenFetchedAt(LocalDateTime.now());
        account.setProvider(GOOGLE_PROVIDER);
    }
}
