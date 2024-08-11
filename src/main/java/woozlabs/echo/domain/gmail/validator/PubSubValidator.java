package woozlabs.echo.domain.gmail.validator;

import org.springframework.stereotype.Component;
import woozlabs.echo.domain.member.entity.FcmToken;
import woozlabs.echo.global.exception.CustomErrorException;
import woozlabs.echo.global.exception.ErrorCode;

import java.util.List;

@Component
public class PubSubValidator {
    private final int MAX_TOKEN_SIZE = 5;
    public void validateSaveFcmToken(List<FcmToken> tokens, String newToken){
        if(tokens.size() < MAX_TOKEN_SIZE){
            for(FcmToken token : tokens){
                if(token.getFcmToken().equals(newToken)){
                    throw new CustomErrorException(ErrorCode.DUPLICATE_FCM_TOKEN_ERR, ErrorCode.DUPLICATE_FCM_TOKEN_ERR.getMessage());
                }
            }
            return;
        }
        throw new CustomErrorException(ErrorCode.EXCEED_FCM_TOKEN_SIZE_ERR, ErrorCode.EXCEED_FCM_TOKEN_SIZE_ERR.getMessage());
    }
}