package woozlabs.echo.domain.organization.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import woozlabs.echo.domain.organization.dto.CreateOrganizationRequest;
import woozlabs.echo.domain.organization.dto.OrganizationResponse;
import woozlabs.echo.domain.organization.service.OrganizationService;
import woozlabs.echo.global.constant.GlobalConstant;

import java.util.List;

@RestController
@RequestMapping("/api/v1/echo")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    @GetMapping("/organizations")
    public ResponseEntity<List<OrganizationResponse>> getOrganizationsByOwner(HttpServletRequest httpServletRequest) {
        String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
        List<OrganizationResponse> organizations = organizationService.getOrganizationsByOwner(uid);
        return ResponseEntity.ok(organizations);
    }

    @PostMapping("/organizations")
    public ResponseEntity<Void> createOrganization(HttpServletRequest httpServletRequest,
                                                   @RequestBody CreateOrganizationRequest createOrganizationRequest) {
        String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
        String organizationName = createOrganizationRequest.getName();
        organizationService.createOrganization(uid, organizationName);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/organizations/{organizationId}/members")
    public ResponseEntity<Void> addMembersToOrganization(
            @PathVariable("organizationId") Long organizationId,
            @RequestBody List<String> memberEmails) {
        organizationService.addMembersToOrganization(organizationId, memberEmails);
        return ResponseEntity.ok().build();
    }
}
