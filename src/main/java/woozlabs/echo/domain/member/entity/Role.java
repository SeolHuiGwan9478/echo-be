package woozlabs.echo.domain.member.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum Role {
    ROLE_USER("ROLE_USER"),
    ROLE_ADMIN("ROLE_ADMIN");

    private final String value;

    Role(String value){
        this.value = value;
    }

    @JsonCreator
    public Role deserializeRole(String value){
        for(Role role : Role.values()){
            if(role.getValue().equals(value)){
                return role;
            }
        }
        return null;
    }

    @JsonValue
    public String serializeRole(){
        return this.value;
    }
}
