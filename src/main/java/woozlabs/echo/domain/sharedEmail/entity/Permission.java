package woozlabs.echo.domain.sharedEmail.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Permission {

    @JsonProperty("Editor")
    EDITOR,

    @JsonProperty("Owner")
    OWNER,

    @JsonProperty("Commenter")
    COMMENTER,

    @JsonProperty("Viewer")
    VIEWER
}
