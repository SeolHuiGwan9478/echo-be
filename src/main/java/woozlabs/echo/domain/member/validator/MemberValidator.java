package woozlabs.echo.domain.member.validator;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import woozlabs.echo.domain.member.entity.MemberAccount;
import woozlabs.echo.domain.member.repository.query.MemberAccountQueryRepository;
import woozlabs.echo.global.constant.GlobalConstant;
import woozlabs.echo.global.exception.CustomErrorException;
import woozlabs.echo.global.exception.ErrorCode;

@Component
@RequiredArgsConstructor
public class MemberValidator {
    private final MemberAccountQueryRepository memberAccountQueryRepository;
    public void validateActiveAccountUid(HttpServletRequest request){
        String uid = (String) request.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
        String aAUid = (String) request.getAttribute(GlobalConstant.ACTIVE_ACCOUNT_UID_KEY);
        MemberAccount memberAccount = memberAccountQueryRepository.findByMemberUidAndAccountUid(uid, aAUid)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_MEMBER_ACCOUNT));
    }
}
