package woozlabs.echo.domain.team.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Role {

    @JsonProperty("Admin")
    ADMIN,

    @JsonProperty("Editor")
    EDITOR,

    @JsonProperty("Viewer")
    VIEWER,

    @JsonProperty("PublicViewer")
    PUBLIC_VIEWER
}
