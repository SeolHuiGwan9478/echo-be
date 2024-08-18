package woozlabs.echo.domain.team.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import woozlabs.echo.domain.team.dto.CreateTeamRequestDto;
import woozlabs.echo.domain.team.dto.TeamInvitationRequestDto;
import woozlabs.echo.domain.team.dto.TeamResponseDto;
import woozlabs.echo.domain.team.service.TeamService;
import woozlabs.echo.global.constant.GlobalConstant;

import java.util.List;

@RestController
@RequestMapping("/api/v1/echo")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @GetMapping("/teams")
    public ResponseEntity<List<TeamResponseDto>> getTeams(HttpServletRequest httpServletRequest) {
        String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
        List<TeamResponseDto> teams = teamService.getTeams(uid);
        return ResponseEntity.ok(teams);
    }

    @PostMapping("/team")
    public ResponseEntity<Void> createTeam(HttpServletRequest httpServletRequest,
                                           @RequestBody CreateTeamRequestDto createTeamRequestDto) {
        String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
        teamService.createTeam(uid, createTeamRequestDto);
        return ResponseEntity.status(201).build();
    }

    @PostMapping("/team/{teamId}/invite")
    public ResponseEntity<Void> inviteToTeam(HttpServletRequest httpServletRequest,
                                             @PathVariable("teamId") Long teamId,
                                             @RequestBody TeamInvitationRequestDto requestDto) {
        String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
        teamService.inviteToTeam(uid, teamId, requestDto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/team/invite/accept")
    public ResponseEntity<Void> acceptInvitation(HttpServletRequest httpServletRequest,
                                                 @RequestParam String token) {
        String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
        teamService.acceptInvitation(uid, token);
        return ResponseEntity.ok().build();
    }
}
