package woozlabs.echo.domain.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import woozlabs.echo.domain.member.dto.profile.AccountProfileResponseDto;
import woozlabs.echo.domain.member.entity.Account;
import woozlabs.echo.domain.member.entity.Member;
import woozlabs.echo.domain.member.entity.MemberAccount;
import woozlabs.echo.domain.member.repository.AccountRepository;
import woozlabs.echo.domain.member.repository.MemberAccountRepository;
import woozlabs.echo.domain.member.repository.MemberRepository;
import woozlabs.echo.global.exception.CustomErrorException;
import woozlabs.echo.global.exception.ErrorCode;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final MemberRepository memberRepository;
    private final MemberAccountRepository memberAccountRepository;

    public AccountProfileResponseDto getProfileByField(String fieldType, String fieldValue) {
        Account account = fetchMemberByField(fieldType, fieldValue);

        return AccountProfileResponseDto.builder()
                .uid(account.getUid())
                .provider(account.getProvider())
                .displayName(account.getDisplayName())
                .profileImageUrl(account.getProfileImageUrl())
                .email(account.getEmail())
                .build();
    }

    private Account fetchMemberByField(String fieldType, String fieldValue) {
        if (fieldType.equals("email")) {
            return accountRepository.findByEmail(fieldValue)
                    .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_ACCOUNT_ERROR_MESSAGE));
        } else if (fieldType.equals("uid")) {
            return accountRepository.findByUid(fieldValue)
                    .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_ACCOUNT_ERROR_MESSAGE));
        } else {
            throw new CustomErrorException(ErrorCode.INVALID_FIELD_TYPE_ERROR_MESSAGE);
        }
    }

    @Transactional
    public void unlinkAccount(String primaryUid, String accountUid) {
        Member member = memberRepository.findByPrimaryUid(primaryUid)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_MEMBER, "Member not found for primaryUid: " + primaryUid));

        Account account = accountRepository.findByUid(accountUid)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_ACCOUNT_ERROR_MESSAGE, "Account not found for accountUid: " + accountUid));

        MemberAccount memberAccount = memberAccountRepository.findByMemberAndAccount(member, account)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_MEMBER_ACCOUNT));

        if (account.getUid().equals(member.getPrimaryUid())) {
            throw new CustomErrorException(ErrorCode.CANNOT_UNLINK_PRIMARY_ACCOUNT);
        }

        member.getMemberAccounts().remove(memberAccount);
        account.getMemberAccounts().remove(memberAccount);

        member.getWatchNotifications().remove(accountUid);

        memberAccountRepository.delete(memberAccount);
        memberRepository.save(member);
        accountRepository.save(account);
    }
}
