package woozlabs.echo.domain.member.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
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
import woozlabs.echo.domain.member.repository.AccountRepository;
import woozlabs.echo.domain.member.repository.MemberAccountRepository;
import woozlabs.echo.domain.member.repository.MemberRepository;
import woozlabs.echo.domain.member.utils.AuthCookieUtils;
import woozlabs.echo.domain.member.utils.AuthUtils;
import woozlabs.echo.global.constant.GlobalConstant;
import woozlabs.echo.global.exception.CustomErrorException;
import woozlabs.echo.global.exception.ErrorCode;
import woozlabs.echo.global.utils.FirebaseTokenVerifier;
import woozlabs.echo.global.utils.GoogleOAuthUtils;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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

    private boolean checkIfEmailExists(String email) throws FirebaseAuthException {
        try {
            UserRecord userRecord = FirebaseAuth.getInstance().getUserByEmail(email);
            return true;
        } catch (FirebaseAuthException e) {
            return false;
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
    public Account createOrUpdateAccount(Map<String, Object> userInfo) throws FirebaseAuthException {
        String providerId = (String) userInfo.get("id");
        String displayName = (String) userInfo.get("name");
        String email = (String) userInfo.get("email");
        String profileImageUrl = (String) userInfo.get("picture");
        String accessToken = (String) userInfo.get("access_token");
        String refreshToken = (String) userInfo.get("refresh_token");
        String provider = GOOGLE_PROVIDER;

        boolean emailExists = checkIfEmailExists(email);
        String uuid;

        if (emailExists) {
            Account existingAccount = accountRepository.findByEmail(email)
                    .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_ACCOUNT_ERROR_MESSAGE));
            uuid = existingAccount.getUid();
        } else {
            uuid = UUID.nameUUIDFromBytes(email.getBytes(StandardCharsets.UTF_8)).toString();
        }

        Account account = accountRepository.findByProviderId(providerId)
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
                        .uid(uuid)
                        .providerId(providerId)
                        .displayName(displayName)
                        .email(email)
                        .profileImageUrl(profileImageUrl)
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .accessTokenFetchedAt(LocalDateTime.now())
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

        Optional<Account> existingAccountOpt = accountRepository.findByProviderId(providerId);

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
                    .orElse(null);

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
        Account newAccount = createOrUpdateAccount(userInfo);

        boolean accountExists = member.getMemberAccounts().stream()
                .anyMatch(ma -> ma.getAccount().equals(newAccount));

        if (!accountExists) {
            MemberAccount memberAccount = new MemberAccount(member, newAccount);
            member.addMemberAccount(memberAccount);
            member.setDeletedAt(null);

            memberAccountRepository.save(memberAccount);

            log.info("Added new account to existing Member. Account UID: {}", newAccount.getUid());
        } else {
            log.info("Account already associated with this member. UID: {}", newAccount.getUid());
        }

        memberRepository.save(member);
        accountRepository.save(newAccount);

        constructAndRedirect(response, createCustomToken(newAccount.getUid()), (String) userInfo.get("name"), (String) userInfo.get("picture"), (String) userInfo.get("email"), true);
    }

    @Transactional
    public void createNewMemberWithAccount(Map<String, Object> userInfo, HttpServletResponse response) throws FirebaseAuthException {
        Account account = createOrUpdateAccount(userInfo);

        String displayName = (String) userInfo.get("name");
        String memberName = displayName + "-" + AuthUtils.generateRandomString();
        String email = (String) userInfo.get("email");

        Member member = new Member();
        member.setPrimaryUid(account.getUid());
        member.setMemberName(memberName);
        member.setDisplayName(displayName);
        member.setEmail(email);
        member.setProfileImageUrl((String) userInfo.get("picture"));

        MemberAccount memberAccount = new MemberAccount(member, account);
        member.addMemberAccount(memberAccount);
        account.getMemberAccounts().add(memberAccount);

        member.setDeletedAt(null);

        String customToken = createCustomToken(account.getUid());

        memberRepository.save(member);
        accountRepository.save(account);

        log.info("Created new Member with a new account. Account UID: {}", account.getUid());

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
