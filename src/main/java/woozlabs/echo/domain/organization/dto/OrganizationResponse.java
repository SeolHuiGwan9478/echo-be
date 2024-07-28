package woozlabs.echo.domain.organization.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import woozlabs.echo.domain.organization.entity.Organization;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationResponse {

    private String organizationName;
    private OrganizationToMemberDto owner;
    private List<String> memberEmails;

    public OrganizationResponse(Organization organization) {
        this.organizationName = organization.getName();
        this.owner = new OrganizationToMemberDto(organization.getOwner());
        this.memberEmails = organization.getEmails();
    }
}
