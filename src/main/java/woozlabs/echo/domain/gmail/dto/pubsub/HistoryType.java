package woozlabs.echo.domain.gmail.dto.pubsub;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum HistoryType {
    MESSAGE_ADDED("MESSAGE_ADDED"),
    MESSAGE_DELETED("MESSAGE_DELETED"),
    LABEL_ADDED("LABEL_ADDED"),
    LABEL_REMOVED("LABEL_REMOVED");

    private final String type;
    HistoryType(String type){
        this.type = type;
    }

    @JsonCreator
    public HistoryType deserializerHistoryType(String value){
        for(HistoryType historyType : HistoryType.values()){
            if(historyType.getType().equals(value)){
                return historyType;
            }
        }
        return null;
    }
    @JsonValue
    public String serializerHistoryType(){
        return type;
    }
}