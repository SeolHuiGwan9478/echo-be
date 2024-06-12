package woozlabs.echo.global.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import woozlabs.echo.domain.member.entity.Member;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class GoogleOauthMemberDetails implements OAuth2User {
    private final Member member;
    private Map<String, Object> attributes; // 사용자 정보

    public GoogleOauthMemberDetails(Member member){
        this.member = member;
    }

    public GoogleOauthMemberDetails(Member member, Map<String, Object> attributes){
        this.member = member;
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add((GrantedAuthority) () -> member.getRole().getValue());
        return authorities;
    }

    @Override
    public String getName() {
        return member.getEmail();
    }
}
