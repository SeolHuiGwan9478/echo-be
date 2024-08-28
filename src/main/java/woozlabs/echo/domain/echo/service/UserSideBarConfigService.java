package woozlabs.echo.domain.echo.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import woozlabs.echo.domain.echo.dto.userSideBar.SidebarNavAccountDto;
import woozlabs.echo.domain.echo.dto.userSideBar.SpaceDto;
import woozlabs.echo.domain.echo.entity.UserSidebarConfig;
import woozlabs.echo.domain.echo.repository.UserSideBarConfigRepository;
import woozlabs.echo.domain.member.entity.Account;
import woozlabs.echo.domain.member.repository.AccountRepository;
import woozlabs.echo.global.exception.CustomErrorException;
import woozlabs.echo.global.exception.ErrorCode;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserSideBarConfigService {

    private final UserSideBarConfigRepository userSideBarConfigRepository;
    private final AccountRepository accountRepository;
    private final ObjectMapper objectMapper;

    /**
     * DB에 저장되어있는 JSON을 역직렬화
     */
    private SidebarNavAccountDto convertToDto(UserSidebarConfig userSidebarConfig) {
        SidebarNavAccountDto dto = new SidebarNavAccountDto();
        dto.setAccountUid(userSidebarConfig.getAccount().getUid());
        try {
            List<SpaceDto> spaces = objectMapper.readValue(userSidebarConfig.getSidebarConfig(), new TypeReference<List<SpaceDto>>() {});
            dto.setSpaces(spaces);
        } catch (IOException e) {
            log.error("Error converting sidebar config JSON to SpaceDto list", e);
            throw new CustomErrorException(ErrorCode.OBJECT_MAPPER_JSON_PARSING_ERROR_MESSAGE, e.getMessage());
        }
        return dto;
    }

    /**
     * 요청 Body로 들어온 JSON을 직렬화 후 처리
     */
    private UserSidebarConfig convertToEntity(SidebarNavAccountDto dto, Account account) {
        UserSidebarConfig config = new UserSidebarConfig();
        config.setAccount(account);
        try {
            String sidebarConfigJson = objectMapper.writeValueAsString(dto.getSpaces());
            config.setSidebarConfig(sidebarConfigJson);
        } catch (IOException e) {
            log.error("Error converting SpaceDto list to sidebar config JSON", e);
            throw new CustomErrorException(ErrorCode.OBJECT_MAPPER_JSON_PARSING_ERROR_MESSAGE, e.getMessage());
        }
        return config;
    }

    public SidebarNavAccountDto getAccountsNavSpace(String uid) {
        Account account = accountRepository.findByUid(uid)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_MEMBER_ERROR_MESSAGE));

        UserSidebarConfig userSidebarConfig = userSideBarConfigRepository.findByAccount(account)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_SIDE_BAR_CONFIG));

        return convertToDto(userSidebarConfig);
    }

    public List<SidebarNavAccountDto> getAllAccountsNavSpace(String uid) {
        Account primaryAccount = accountRepository.findByUid(uid)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_MEMBER_ERROR_MESSAGE));

        List<Account> linkedAccounts = accountRepository.findAllByMember(primaryAccount.getMember());
        return linkedAccounts.stream()
                .map(member -> userSideBarConfigRepository.findByAccount(member)
                        .map(this::convertToDto)
                        .orElse(null))
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }

    @Transactional
    public void saveConfig(String uid, List<SidebarNavAccountDto> dtos) {
        Account primaryAccount = accountRepository.findByUid(uid)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_MEMBER_ERROR_MESSAGE));

        for (SidebarNavAccountDto dto : dtos) {
            Account dtoAccount = accountRepository.findByUid(dto.getAccountUid())
                    .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_MEMBER_ERROR_MESSAGE, "Account not found for accountUid: " + dto.getAccountUid()));

            if (!dtoAccount.getMember().equals(primaryAccount.getMember())) {
                throw new CustomErrorException(ErrorCode.INVALID_ACCOUNT_UID, "Account UID " + dto.getAccountUid() + " is not linked to the primary account's super account.");
            }

            UserSidebarConfig existingConfig = userSideBarConfigRepository.findByAccount(dtoAccount)
                    .orElse(new UserSidebarConfig());

            UserSidebarConfig newConfig = convertToEntity(dto, dtoAccount);
            existingConfig.setSidebarConfig(newConfig.getSidebarConfig());

            existingConfig.setAccount(dtoAccount);
            log.info("Saving sidebar config for account: {}", dtoAccount.getUid());
            userSideBarConfigRepository.save(existingConfig);
        }
    }
}
