package woozlabs.echo.domain.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import woozlabs.echo.domain.member.dto.CreateAccountRequestDto;
import woozlabs.echo.domain.member.entity.Member;
import woozlabs.echo.domain.member.entity.Role;
import woozlabs.echo.domain.member.entity.SuperAccount;
import woozlabs.echo.domain.member.repository.MemberRepository;
import woozlabs.echo.domain.member.repository.SubAccountRepository;
import woozlabs.echo.domain.member.repository.SuperAccountRepository;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final SuperAccountRepository superAccountRepository;
    private final SubAccountRepository subAccountRepository;

    @Transactional
    public void createAccount(CreateAccountRequestDto requestDto) {
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
                .superToken(requestDto.getSuperToken())
                .googleAccessToken(requestDto.getGoogleAccessToken())
                .member(member)
                .build();

        superAccountRepository.save(superAccount);
    }
}
