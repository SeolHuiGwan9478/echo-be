package woozlabs.echo.domain.member.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;
import woozlabs.echo.domain.member.entity.SuperAccount;
import woozlabs.echo.domain.member.entity.Member;
import woozlabs.echo.domain.member.entity.Role;
import woozlabs.echo.domain.member.repository.SuperAccountRepository;
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

    private final MemberRepository memberRepository;
    private final SuperAccountRepository superAccountRepository;
    private final FirebaseTokenVerifier firebaseTokenVerifier;
    private final GoogleOAuthUtils googleOAuthUtils;

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
    public Member createOrUpdateMember(Map<String, Object> userInfo, boolean isPrimary) {
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
                        .isPrimary(isPrimary)
                        .build());

        return memberRepository.save(member);
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

        Optional<String> superAccountUidOpt = AuthCookieUtils.getCookieValue(request);
        String superAccountUid = superAccountUidOpt.orElse(null);

        if (superAccountUid == null) {
            log.info("Cookie 'superAccountUid' is null. Creating new member and super account.");

            Member member = createOrUpdateMember(userInfo, true);
            log.info("Created or updated member with UID: {}", member.getUid());

            SuperAccount superAccount = superAccountRepository.findByMemberUids(member.getUid())
                    .orElseGet(() -> {
                        SuperAccount newSuperAccount = new SuperAccount();
                        newSuperAccount.getMembers().add(member);
                        newSuperAccount.getMemberUids().add(member.getUid());
                        return superAccountRepository.save(newSuperAccount);
                    });

            if (!superAccount.getMembers().contains(member)) {
                superAccount.getMembers().add(member);
            }
            if (!superAccount.getMemberUids().contains(member.getUid())) {
                superAccount.getMemberUids().add(member.getUid());
            }

            member.setSuperAccount(superAccount);
            memberRepository.save(member);

            Map<String, Object> customClaims = Map.of("accounts", superAccount.getMemberUids());
            setCustomUidClaims(member.getUid(), customClaims);

            AuthCookieUtils.addCookie(response, member.getUid());
            log.info("New SuperAccount created with ID: {}", superAccount.getId());
            log.info("Member added to SuperAccount. Member UID: {}", member.getUid());
            log.info("Cookie attribute 'superAccountUid' set to: {}", member.getUid());
        } else {
            log.info("Cookie 'superAccountUid' found. Adding new member to existing SuperAccount.");

            SuperAccount superAccount = superAccountRepository.findByMemberUids(superAccountUid)
                    .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_SUPER_ACCOUNT));

            Member newMember = createOrUpdateMember(userInfo, false);
            log.info("Created or updated new member with UID: {}", newMember.getUid());

            newMember.setSuperAccount(superAccount);
            memberRepository.save(newMember);

            if (!superAccount.getMembers().contains(newMember)) {
                superAccount.getMembers().add(newMember);
            }
            if (!superAccount.getMemberUids().contains(newMember.getUid())) {
                superAccount.getMemberUids().add(newMember.getUid());
            }
            superAccountRepository.save(superAccount);

            Map<String, Object> customClaims = Map.of("accounts", superAccount.getMemberUids());
            setCustomUidClaims(superAccountUid, customClaims);

            log.info("Added new member to existing SuperAccount. Member UID: {}", newMember.getUid());
        }

        constructAndRedirect(response, createCustomToken(providerId), (String) userInfo.get("name"), (String) userInfo.get("picture"), (String) userInfo.get("email"));
    }
}
