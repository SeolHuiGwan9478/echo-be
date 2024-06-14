package woozlabs.echo.domain.member.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import woozlabs.echo.domain.member.entity.Member;
import woozlabs.echo.domain.member.entity.Role;
import woozlabs.echo.domain.member.repository.MemberRepository;
import woozlabs.echo.global.security.GoogleOauthMemberDetails;

import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String oauthClientName = userRequest.getClientRegistration().getClientName();

        try {
            log.info("OAuth2 User Attributes: {}", new ObjectMapper().writeValueAsString(oAuth2User.getAttributes()));
        } catch (Exception exception) {
            log.error("Failed to serialize OAuth2User: {}", exception.getMessage());
        }

        if (oauthClientName.equals("Google")) {
            Map<String, Object> attributes = oAuth2User.getAttributes();
            String providerId = attributes.get("sub").toString();
            Member member = memberRepository.findByProviderAndProviderId("google", providerId);
            if (member == null) {
                member = createUserFromOAuth2User(oAuth2User, providerId);
            }
            return new GoogleOauthMemberDetails(member, attributes);
        }

        return oAuth2User;
    }

    private Member createUserFromOAuth2User(OAuth2User oAuth2User, String providerId) {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = attributes.get("email").toString();
        String name = attributes.get("name").toString();
        String profileImage = attributes.get("picture").toString();

        Optional<Member> optionalMember = Optional.ofNullable(memberRepository.findByProviderAndProviderId("google", providerId));
        if (optionalMember.isPresent()) {
            return optionalMember.get();
        } else {
            Member member = Member.builder()
                    .email(email)
                    .name(name)
                    .role(Role.ROLE_USER)
                    .provider("google")
                    .providerId(providerId)
                    .profileImage(profileImage)
                    .build();

            return memberRepository.save(member);
        }
    }
}