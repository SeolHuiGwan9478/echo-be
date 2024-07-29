package woozlabs.echo.domain.contactGroup.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import woozlabs.echo.domain.contactGroup.entity.ContactGroup;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactGroupResponse {

    private String contactGroupName;
    private ContactGroupToMember owner;
    private List<String> memberEmails;

    public ContactGroupResponse(ContactGroup contactGroup) {
        this.contactGroupName = contactGroup.getName();
        this.owner = new ContactGroupToMember(contactGroup.getOwner());
        this.memberEmails = contactGroup.getEmails();
    }
}
