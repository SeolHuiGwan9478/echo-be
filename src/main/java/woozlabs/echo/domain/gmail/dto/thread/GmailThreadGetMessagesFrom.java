package woozlabs.echo.domain.gmail.dto.thread;

import lombok.Builder;
import lombok.Getter;

import java.util.Objects;

@Getter
@Builder
public class GmailThreadGetMessagesFrom {
    private String name;
    private String email;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GmailThreadGetMessagesFrom that = (GmailThreadGetMessagesFrom) o;
        return this.name.equals(that.getName()) &&
                this.email.equals(that.getEmail());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, email);
    }
}