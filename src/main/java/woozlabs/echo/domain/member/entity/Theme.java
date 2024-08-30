package woozlabs.echo.domain.member.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Theme {

    @JsonProperty("light")
    LIGHT,

    @JsonProperty("dark")
    DARK,

    @JsonProperty("system")
    SYSTEM
}
