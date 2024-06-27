package woozlabs.echo.global.token.entity;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.redis.core.RedisHash;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@RedisHash(value = "accessToken", timeToLive = 3600)
public class AccessToken {

    @Id
    private String accessToken;
    private Long memberId;
}
