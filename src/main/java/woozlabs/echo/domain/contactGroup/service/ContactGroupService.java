package woozlabs.echo.domain.contactGroup.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import woozlabs.echo.domain.contactGroup.dto.ContactGroupResponse;
import woozlabs.echo.domain.contactGroup.entity.ContactGroup;
import woozlabs.echo.domain.contactGroup.repository.ContactGroupRepository;
import woozlabs.echo.domain.member.entity.Member;
import woozlabs.echo.domain.member.repository.MemberRepository;
import woozlabs.echo.global.exception.CustomErrorException;
import woozlabs.echo.global.exception.ErrorCode;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContactGroupService {

    private final ContactGroupRepository contactGroupRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void createContactGroup(String ownerUid, String contactGroupName) {
        Member owner = memberRepository.findByUid(ownerUid)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_MEMBER_ERROR_MESSAGE));

        ContactGroup contactGroup = new ContactGroup();
        contactGroup.setName(contactGroupName);
        contactGroup.setOwner(owner);

        contactGroup.addMember(owner);
        contactGroupRepository.save(contactGroup);
    }

    @Transactional
    public void addMembersToContactGroup(Long contactGroupId, List<String> memberEmails) {
        ContactGroup contactGroup = contactGroupRepository.findById(contactGroupId)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_ORGANIZATION));

        for (String memberEmail : memberEmails) {
            contactGroup.addEmail(memberEmail);
        }
        contactGroupRepository.save(contactGroup);
    }

    public List<ContactGroupResponse> getContactGroupsByOwner(String ownerUid) {
        Member owner = memberRepository.findByUid(ownerUid)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_MEMBER_ERROR_MESSAGE));

        List<ContactGroup> contactGroups = contactGroupRepository.findByOwner(owner);
        return contactGroups.stream()
                .map(ContactGroupResponse::new)
                .collect(Collectors.toList());
    }
}
