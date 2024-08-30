package woozlabs.echo.domain.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import woozlabs.echo.domain.member.dto.*;
import woozlabs.echo.domain.member.entity.Account;
import woozlabs.echo.domain.member.repository.AccountRepository;
import woozlabs.echo.global.exception.CustomErrorException;
import woozlabs.echo.global.exception.ErrorCode;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountProfileResponseDto getProfileByField(String fieldType, String fieldValue) {
        Account account = fetchMemberByField(fieldType, fieldValue);

        return AccountProfileResponseDto.builder()
                .uid(account.getUid())
                .provider(account.getProvider())
                .displayName(account.getDisplayName())
                .profileImageUrl(account.getProfileImageUrl())
                .email(account.getEmail())
                .isPrimary(account.isPrimary())
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
}
