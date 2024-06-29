package woozlabs.echo.global.token.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@RedisHash(value = "accessToken", timeToLive = 3600)
public class AccessToken {

    @Id
    private Long memberId;
    private String accessToken;
}
