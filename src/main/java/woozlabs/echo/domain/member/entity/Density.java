package woozlabs.echo.domain.member.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Density {

    @JsonProperty("compact")
    COMPACT,

    @JsonProperty("cozy")
    COZY
}
