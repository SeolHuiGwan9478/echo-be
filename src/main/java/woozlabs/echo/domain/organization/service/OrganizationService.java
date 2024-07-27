package woozlabs.echo.domain.organization.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import woozlabs.echo.domain.member.entity.Member;
import woozlabs.echo.domain.member.repository.MemberRepository;
import woozlabs.echo.domain.organization.dto.OrganizationResponse;
import woozlabs.echo.domain.organization.entity.Organization;
import woozlabs.echo.domain.organization.repository.OrganizationRepository;
import woozlabs.echo.global.exception.CustomErrorException;
import woozlabs.echo.global.exception.ErrorCode;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final MemberRepository memberRepository;

    public List<OrganizationResponse> getOrganizationsByOwner(String ownerUid) {
        Member owner = memberRepository.findByUid(ownerUid)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_MEMBER_ERROR_MESSAGE));

        List<Organization> organizations = organizationRepository.findByOwner(owner);
        return organizations.stream()
                .map(OrganizationResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void createOrganization(String ownerUid, String organizationName) {
        Member owner = memberRepository.findByUid(ownerUid)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_MEMBER_ERROR_MESSAGE));

        Organization organization = new Organization();
        organization.setName(organizationName);
        organization.setOwner(owner);

        organization.addMember(owner);
        organizationRepository.save(organization);
    }

    @Transactional
    public void addMembersToOrganization(Long organizationId, List<String> memberEmails) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_ORGANIZATION));

        for (String email : memberEmails) {
            organization.addEmail(email);
        }

        organizationRepository.save(organization);
    }
}
