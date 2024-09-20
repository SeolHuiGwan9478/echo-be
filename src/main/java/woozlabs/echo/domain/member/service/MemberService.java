package woozlabs.echo.domain.member.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import woozlabs.echo.domain.member.dto.*;
import woozlabs.echo.domain.member.entity.Account;
import woozlabs.echo.domain.member.entity.Member;
import woozlabs.echo.domain.member.entity.MemberAccount;
import woozlabs.echo.domain.member.repository.AccountRepository;
import woozlabs.echo.domain.member.repository.MemberAccountRepository;
import woozlabs.echo.domain.member.repository.MemberRepository;
import woozlabs.echo.domain.member.utils.AuthUtils;
import woozlabs.echo.global.exception.CustomErrorException;
import woozlabs.echo.global.exception.ErrorCode;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final AccountRepository accountRepository;
    private final MemberRepository memberRepository;
    private final MemberAccountRepository memberAccountRepository;

    @Transactional
    public void updatePreference(String primaryUid, UpdatePreferenceRequestDto updatePreferenceRequest) {
        Member member = memberRepository.findByPrimaryUid(primaryUid)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_MEMBER));

        PreferenceDto preferenceDto = updatePreferenceRequest.getPreference();
        if (preferenceDto != null) {
            if (preferenceDto.getLanguage() != null) {
                member.setLanguage(preferenceDto.getLanguage());
            }

            AppearanceDto appearanceDto = preferenceDto.getAppearance();
            if (appearanceDto != null) {
                if (appearanceDto.getTheme() != null) {
                    member.setTheme(appearanceDto.getTheme());
                }
                if (appearanceDto.getDensity() != null) {
                    member.setDensity(appearanceDto.getDensity());
                }
            }

            NotificationDto notificationDto = preferenceDto.getNotification();
            if (notificationDto != null) {
                if (notificationDto.getWatchNotification() != null) {
                    member.setWatchNotification(notificationDto.getWatchNotification());
                }
                if (notificationDto.getMarketingEmails() != null) {
                    member.setMarketingEmails(notificationDto.getMarketingEmails());
                }
                if (notificationDto.getSecurityEmails() != null) {
                    member.setSecurityEmails(notificationDto.getSecurityEmails());
                }
            }
        }
    }

    public PreferenceDto getPreference(String primaryUid) {
        Member member = memberRepository.findByPrimaryUid(primaryUid)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_MEMBER));

        return PreferenceDto.builder()
                .language(member.getLanguage())
                .appearance(AppearanceDto.builder()
                        .theme(member.getTheme())
                        .density(member.getDensity())
                        .build())
                .notification(NotificationDto.builder()
                        .watchNotification(member.getWatchNotification())
                        .marketingEmails(member.isMarketingEmails())
                        .securityEmails(member.isSecurityEmails())
                        .build())
                .build();
    }

    @Transactional
    public void deleteMember(String primaryUid) {
        Member member = memberRepository.findByPrimaryUid(primaryUid)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_MEMBER));

        member.setDeletedAt(LocalDateTime.now());

        List<MemberAccount> memberAccounts = member.getMemberAccounts();
        for (MemberAccount memberAccount : memberAccounts) {
            Account account = memberAccount.getAccount();
            account.getMemberAccounts().remove(memberAccount);
            accountRepository.save(account);
        }

        member.getMemberAccounts().clear();
        memberRepository.save(member);
    }

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void hardDeleteExpiredMembers() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Member> expiredMember = memberRepository.findAllByDeletedAtBefore(thirtyDaysAgo);

        for (Member member : expiredMember) {
            List<Account> accountsToDelete = memberAccountRepository.findAllAccountsByMember(member);
            for (Account account : accountsToDelete) {
                List<MemberAccount> otherMemberAccounts = memberAccountRepository.findByAccount(account);
                if (otherMemberAccounts.size() == 1) { // 이 계정이 다른 멤버와 연결되지 않은 경우에만 삭제
                    accountRepository.delete(account);
                }
                try {
                    FirebaseAuth.getInstance().deleteUser(account.getUid());
                } catch (FirebaseAuthException e) {
                    throw new CustomErrorException(ErrorCode.FIREBASE_ACCOUNT_DELETION_ERROR, e.getMessage());
                }
            }

            memberRepository.delete(member);
        }
    }

    @Transactional
    public void hardDeleteMember(String primaryUid) {
        Member member = memberRepository.findByPrimaryUid(primaryUid)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_MEMBER));

        try {
            FirebaseAuth.getInstance().deleteUser(member.getPrimaryUid());
        } catch (FirebaseAuthException e) {
            throw new CustomErrorException(ErrorCode.FIREBASE_ACCOUNT_DELETION_ERROR, e.getMessage());
        }

        List<Account> accountsToDelete = new ArrayList<>();

        List<MemberAccount> memberAccounts = new ArrayList<>(member.getMemberAccounts());
        for (MemberAccount memberAccount : memberAccounts) {
            Account account = memberAccount.getAccount();
            account.getMemberAccounts().remove(memberAccount);
            member.getMemberAccounts().remove(memberAccount);

            memberAccountRepository.delete(memberAccount);

            // 해당 계정이 다른 멤버와 연결되어 있지 않으면 삭제 대상에 추가
            if (account.getMemberAccounts().isEmpty()) {
                accountsToDelete.add(account);
            }
        }

        memberRepository.delete(member);
        accountRepository.deleteAll(accountsToDelete);

        log.info("Successfully deleted member with UID: {}", primaryUid);
    }

    public Object getAccountInfo(String uid) {
        Account currentAccount = accountRepository.findByUid(uid)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_ACCOUNT_ERROR_MESSAGE));

        List<MemberAccount> memberAccounts = memberAccountRepository.findByAccount(currentAccount);
        if (memberAccounts.isEmpty()) {
            throw new CustomErrorException(ErrorCode.NOT_FOUND_MEMBER_ACCOUNT);
        }

        Optional<Member> primaryMember = memberAccounts.stream()
                .map(MemberAccount::getMember)
                .filter(member -> member.getPrimaryUid().equals(uid))
                .findFirst();

        boolean isPrimaryAccount = primaryMember.isPresent();
        Member firstMember = isPrimaryAccount ? primaryMember.get() : memberAccounts.get(0).getMember();

        if (isPrimaryAccount) {
            List<Account> accounts = memberAccountRepository.findAllAccountsByMember(firstMember);

            GetPrimaryAccountResponseDto.MemberDto memberDto = GetPrimaryAccountResponseDto.MemberDto.builder()
                    .displayName(firstMember.getDisplayName())
                    .memberName(firstMember.getMemberName())
                    .email(firstMember.getEmail())
                    .primaryUid(firstMember.getPrimaryUid())
                    .profileImageUrl(firstMember.getProfileImageUrl())
                    .createdAt(firstMember.getCreatedAt())
                    .updatedAt(firstMember.getUpdatedAt())
                    .build();

            List<GetPrimaryAccountResponseDto.AccountDto> accountDtos = accounts.stream()
                    .map(account -> GetPrimaryAccountResponseDto.AccountDto.builder()
                            .uid(account.getUid())
                            .email(account.getEmail())
                            .displayName(account.getDisplayName())
                            .profileImageUrl(account.getProfileImageUrl())
                            .provider(account.getProvider())
                            .build())
                    .collect(Collectors.toList());

            List<GetPrimaryAccountResponseDto.RelatedMemberDto> relatedMembers = memberAccounts.stream()
                    .map(MemberAccount::getMember)
                    .filter(member -> !member.getId().equals(firstMember.getId()))
                    .map(member -> GetPrimaryAccountResponseDto.RelatedMemberDto.builder()
                            .displayName(member.getDisplayName())
                            .memberName(member.getMemberName())
                            .email(member.getEmail())
                            .primaryUid(member.getPrimaryUid())
                            .profileImageUrl(member.getProfileImageUrl())
                            .createdAt(member.getCreatedAt())
                            .updatedAt(member.getUpdatedAt())
                            .build())
                    .collect(Collectors.toList());

            return GetPrimaryAccountResponseDto.builder()
                    .member(memberDto)
                    .accounts(accountDtos)
                    .relatedMembers(relatedMembers)
                    .build();
        } else {
            GetAccountResponseDto.AccountDto currentAccountDto = GetAccountResponseDto.AccountDto.builder()
                    .uid(currentAccount.getUid())
                    .email(currentAccount.getEmail())
                    .displayName(currentAccount.getDisplayName())
                    .profileImageUrl(currentAccount.getProfileImageUrl())
                    .provider(currentAccount.getProvider())
                    .build();

            List<GetAccountResponseDto.RelatedMemberDto> relatedMembers = memberAccounts.stream()
                    .map(MemberAccount::getMember)
                    .map(member -> GetAccountResponseDto.RelatedMemberDto.builder()
                            .displayName(member.getDisplayName())
                            .memberName(member.getMemberName())
                            .email(member.getEmail())
                            .primaryUid(member.getPrimaryUid())
                            .profileImageUrl(member.getProfileImageUrl())
                            .createdAt(member.getCreatedAt())
                            .updatedAt(member.getUpdatedAt())
                            .build())
                    .collect(Collectors.toList());

            return GetAccountResponseDto.builder()
                    .accounts(Collections.singletonList(currentAccountDto))
                    .relatedMembers(relatedMembers)
                    .build();
        }
    }

    @Transactional
    public GetPrimaryAccountResponseDto createMember(String uid) {
        Account account = accountRepository.findByUid(uid)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_ACCOUNT_ERROR_MESSAGE));

        List<MemberAccount> memberAccounts = memberAccountRepository.findByAccount(account);

        String memberName = account.getDisplayName() + "-" + AuthUtils.generateRandomString();

        Member newMember = Member.builder()
                .displayName(account.getDisplayName())
                .memberName(memberName)
                .email(account.getEmail())
                .primaryUid(account.getUid())
                .profileImageUrl(account.getProfileImageUrl())
                .primaryUid(account.getUid())
                .build();

        memberRepository.save(newMember);

        List<Member> relatedMembers = memberAccounts.stream()
                .map(MemberAccount::getMember)
                .filter(member -> !member.getId().equals(newMember.getId()))
                .collect(Collectors.toList());

        List<GetPrimaryAccountResponseDto.RelatedMemberDto> relatedMemberDtos = relatedMembers.stream()
                .map(relatedMember -> GetPrimaryAccountResponseDto.RelatedMemberDto.builder()
                        .displayName(relatedMember.getDisplayName())
                        .memberName(relatedMember.getMemberName())
                        .email(relatedMember.getEmail())
                        .primaryUid(relatedMember.getPrimaryUid())
                        .profileImageUrl(relatedMember.getProfileImageUrl())
                        .createdAt(relatedMember.getCreatedAt())
                        .updatedAt(relatedMember.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());

        GetPrimaryAccountResponseDto.MemberDto memberDto = GetPrimaryAccountResponseDto.MemberDto.builder()
                .displayName(newMember.getDisplayName())
                .memberName(newMember.getMemberName())
                .email(newMember.getEmail())
                .primaryUid(newMember.getPrimaryUid())
                .profileImageUrl(newMember.getProfileImageUrl())
                .createdAt(newMember.getCreatedAt())
                .updatedAt(newMember.getUpdatedAt())
                .build();

        return GetPrimaryAccountResponseDto.builder()
                .member(memberDto)
                .accounts(Collections.singletonList(GetPrimaryAccountResponseDto.AccountDto.builder()
                        .uid(account.getUid())
                        .email(account.getEmail())
                        .displayName(account.getDisplayName())
                        .profileImageUrl(account.getProfileImageUrl())
                        .provider(account.getProvider())
                        .build()))
                .relatedMembers(relatedMemberDtos)
                .build();
    }
}
