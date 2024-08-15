package woozlabs.echo.domain.gmail.dto.pubsub;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum HistoryType {
    MESSAGE_ADDED("messageAdded"),
    MESSAGE_DELETED("messageDeleted"),
    LABEL_ADDED("labelAdded"),
    LABEL_REMOVED("labelRemoved");

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