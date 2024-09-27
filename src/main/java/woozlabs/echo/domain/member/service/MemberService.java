package woozlabs.echo.domain.member.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import woozlabs.echo.domain.member.dto.GetAccountResponseDto;
import woozlabs.echo.domain.member.dto.GetPrimaryAccountResponseDto;
import woozlabs.echo.domain.member.dto.preference.AppearanceDto;
import woozlabs.echo.domain.member.dto.preference.NotificationDto;
import woozlabs.echo.domain.member.dto.preference.PreferenceDto;
import woozlabs.echo.domain.member.dto.preference.UpdatePreferenceRequestDto;
import woozlabs.echo.domain.member.dto.profile.ChangeProfileRequestDto;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
    public void softDeleteMember(String primaryUid) {
        Member member = memberRepository.findByPrimaryUid(primaryUid)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_MEMBER));

        member.setDeletedAt(LocalDateTime.now());
        memberRepository.save(member);
    }

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void hardDeleteExpiredMembers() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        // 1. Find expired members
        List<Long> expiredMemberIds = memberRepository.findExpiredMemberIds(thirtyDaysAgo);

        if (expiredMemberIds.isEmpty()) {
            log.info("No expired members found");
            return;
        }

        // 2. Find accounts associated only with expired members
        List<String> accountUidsToDelete = accountRepository.findUidsAssociatedOnlyWithExpiredMembers(expiredMemberIds);

        // 3. Bulk delete MemberAccounts for expired members
        int deletedMemberAccounts = memberAccountRepository.bulkDeleteByMemberIds(expiredMemberIds);
        log.info("Deleted {} MemberAccounts for expired members", deletedMemberAccounts);

        // 4. Bulk delete expired Members
        int deletedMembers = memberRepository.bulkDeleteByIds(expiredMemberIds);
        log.info("Hard deleted {} expired members", deletedMembers);

        // 5. Bulk delete Accounts associated only with expired members
        int deletedAccounts = accountRepository.bulkDeleteByUids(accountUidsToDelete);
        log.info("Deleted {} associated accounts", deletedAccounts);

        // 6. Delete Firebase accounts
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        for (String uid : accountUidsToDelete) {
            try {
                firebaseAuth.deleteUser(uid);
                log.info("Deleted Firebase account: {}", uid);
            } catch (FirebaseAuthException e) {
                log.error("Failed to delete Firebase account: " + uid, e);
            }
        }
    }

    @Transactional
    public void superHardDeleteMember(String primaryUid) {
        Member member = memberRepository.findByPrimaryUid(primaryUid)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_MEMBER));

        List<Account> accountsToDelete = new ArrayList<>();
        List<MemberAccount> memberAccountsToDelete = new ArrayList<>();

        for (MemberAccount memberAccount : member.getMemberAccounts()) {
            Account account = memberAccount.getAccount();
            account.getMemberAccounts().remove(memberAccount);
            memberAccountsToDelete.add(memberAccount);

            if (account.getMemberAccounts().isEmpty()) {
                accountsToDelete.add(account);
            }
        }

        member.getMemberAccounts().clear();
        memberAccountRepository.deleteAll(memberAccountsToDelete);
        memberRepository.delete(member);

        for (Account account : accountsToDelete) {
            try {
                FirebaseAuth.getInstance().deleteUser(account.getUid());
            } catch (FirebaseAuthException e) {
                throw new CustomErrorException(ErrorCode.FIREBASE_ACCOUNT_DELETION_ERROR, e.getMessage());
            }
        }

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

        newMember.setMemberAccounts(new ArrayList<>());

        memberRepository.save(newMember);

        MemberAccount newMemberAccount = new MemberAccount(newMember, account);
        newMember.addMemberAccount(newMemberAccount);
        account.getMemberAccounts().add(newMemberAccount);

        memberAccountRepository.save(newMemberAccount);

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

    @Transactional
    public void changeProfile(String primaryUid, ChangeProfileRequestDto changeProfileRequestDto) {
        Member member = memberRepository.findByPrimaryUid(primaryUid)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_MEMBER));

        String displayName = changeProfileRequestDto.getDisplayName();
        String profileImageUrl = changeProfileRequestDto.getProfileImageUrl();
        String language = changeProfileRequestDto.getLanguage();

        if (displayName != null) {
            member.setDisplayName(displayName);
        }
        if (profileImageUrl != null) {
            member.setProfileImageUrl(profileImageUrl);
        }
        if (language != null) {
            member.setLanguage(language);
        }
    }
}
