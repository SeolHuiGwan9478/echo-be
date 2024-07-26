package woozlabs.echo.domain.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import woozlabs.echo.domain.member.dto.LinkedAccountDto;
import woozlabs.echo.domain.member.dto.MemberDto;
import woozlabs.echo.domain.member.dto.ProfileResponseDto;
import woozlabs.echo.domain.member.entity.Member;
import woozlabs.echo.domain.member.entity.SuperAccount;
import woozlabs.echo.domain.member.repository.MemberRepository;
import woozlabs.echo.domain.member.repository.SuperAccountRepository;
import woozlabs.echo.global.exception.CustomErrorException;
import woozlabs.echo.global.exception.ErrorCode;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final SuperAccountRepository superAccountRepository;

    public ProfileResponseDto getProfileByEmail(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_MEMBER_ERROR_MESSAGE));

        return ProfileResponseDto.builder()
                .displayName(member.getDisplayName())
                .email(member.getEmail())
                .profileImageUrl(member.getProfileImageUrl())
                .build();
    }

    public MemberDto getMemberWithLinkedAccounts(String uid) {
        Member primaryMember = memberRepository.findByUid(uid)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_MEMBER_ERROR_MESSAGE));

        SuperAccount superAccount = primaryMember.getSuperAccount();
        List<Member> allLinkedMembers = memberRepository.findAllBySuperAccount(superAccount);

        List<LinkedAccountDto> linkedAccounts = allLinkedMembers.stream()
                .filter(m -> !m.getUid().equals(uid))
                .map(LinkedAccountDto::new)
                .collect(Collectors.toList());

        return new MemberDto(primaryMember, linkedAccounts);
    }
}
