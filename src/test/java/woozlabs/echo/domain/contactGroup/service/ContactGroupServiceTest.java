package woozlabs.echo.domain.contactGroup.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import woozlabs.echo.domain.contactGroup.dto.ContactGroupResponse;
import woozlabs.echo.domain.contactGroup.entity.ContactGroup;
import woozlabs.echo.domain.contactGroup.repository.ContactGroupRepository;
import woozlabs.echo.domain.member.entity.Member;
import woozlabs.echo.domain.member.repository.MemberRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContactGroupServiceTest {

    @InjectMocks
    private ContactGroupService contactGroupService;

    @Mock
    private ContactGroupRepository contactGroupRepository;

    @Mock
    private MemberRepository memberRepository;

    private Member mockMember;
    private ContactGroup mockContactGroup;

    @BeforeEach
    void setUp() {
        mockMember = new Member();
        mockMember.setId(1L);
        mockMember.setUid("1234567891");

        mockContactGroup = new ContactGroup();
        mockContactGroup.setId(1L);
        mockContactGroup.setName("Test ContactGroup");
        mockContactGroup.setOwner(mockMember);
    }

    @Test
    @DisplayName("ContactGroup을 생성합니다.")
    void createContactGroup() throws Exception {
        // given
        String ownerUid = "123456891";
        String contactGroupName = "New Group";

        doReturn(Optional.of(mockMember)).when(memberRepository).findByUid(ownerUid);

        // when
        contactGroupService.createContactGroup(ownerUid, contactGroupName);

        // then
        // ArgumentCaptor를 사용해 save 메서드에 전달된 ContactGroup을 캡처
        ArgumentCaptor<ContactGroup> contactGroupCaptor = ArgumentCaptor.forClass(ContactGroup.class);
        verify(contactGroupRepository).save(contactGroupCaptor.capture());
        ContactGroup savedGroup = contactGroupCaptor.getValue();

        assertThat(savedGroup.getName()).isEqualTo(contactGroupName);
        assertThat(savedGroup.getOwner()).isEqualTo(mockMember);

        // Verify memberRepository.findByUid 호출 여부 검증
        verify(memberRepository, times(1)).findByUid(ownerUid);

        // Verify contactGroupRepository.save 호출 여부 검증
        verify(contactGroupRepository, times(1)).save(any(ContactGroup.class));
    }

    @Test
    @DisplayName("ContactGroup에 멤버를 추가합니다.")
    void addMembersToContactGroup() throws Exception {
        // given
        Long contactGroupId = 1L;
        List<String> memberEmails = List.of("ldhbenecia@echo.com", "seoulhuigwan@echo.com");

        doReturn(Optional.of(mockContactGroup)).when(contactGroupRepository).findById(contactGroupId);
        // when
        contactGroupService.addMembersToContactGroup(contactGroupId, memberEmails);

        // then
        verify(contactGroupRepository, times(1)).findById(contactGroupId);
        verify(contactGroupRepository, times(1)).save(mockContactGroup);
    }

    @Test
    @DisplayName("소유자의 ContactGroup 목록을 가져옵니다.")
    void getContactGroupsByOwner() throws Exception {
        // given
        String ownerUid = "123456891";
        List<ContactGroup> mockContactGroups = List.of(mockContactGroup);

        doReturn(Optional.of(mockMember)).when(memberRepository).findByUid(ownerUid);
        doReturn(mockContactGroups).when(contactGroupRepository).findByOwner(mockMember);

        // when
        List<ContactGroupResponse> contactGroupResponses = contactGroupService.getContactGroupsByOwner(ownerUid);

        // then
        assertThat(contactGroupResponses).hasSize(1);

        verify(memberRepository, times(1)).findByUid(ownerUid);
        verify(contactGroupRepository, times(1)).findByOwner(mockMember);
    }
}