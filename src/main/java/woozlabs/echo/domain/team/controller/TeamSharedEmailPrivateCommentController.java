package woozlabs.echo.domain.team.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import woozlabs.echo.domain.team.dto.TeamSharedEmailPrivateCommentCreateDto;
import woozlabs.echo.domain.team.dto.TeamSharedEmailPrivateCommentResponseDto;
import woozlabs.echo.domain.team.dto.UserPublicKeyDto;
import woozlabs.echo.domain.team.service.TeamSharedEmailPrivateCommentService;
import woozlabs.echo.global.constant.GlobalConstant;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/echo")
public class TeamSharedEmailPrivateCommentController {

    private final TeamSharedEmailPrivateCommentService teamSharedEmailPrivateCommentService;

//    @GetMapping("/shared-email/{sharedEmailId}")
//    public ResponseEntity<List<TeamSharedEmailPrivateCommentResponseDto>> getCommentsBySharedEmailId(HttpServletRequest httpServletRequest,
//                                                                                                     @PathVariable("sharedEmailId") String sharedEmailId) {
//        String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
//        List<TeamSharedEmailPrivateCommentResponseDto> comments = teamSharedEmailPrivateCommentService.getCommentsBySharedEmailId(uid, sharedEmailId);
//        return ResponseEntity.ok(comments);
//    }
//
//    @PostMapping("/shared-email")
//    public ResponseEntity<Void> createComment(HttpServletRequest httpServletRequest,
//                                              @RequestBody TeamSharedEmailPrivateCommentCreateDto createDto) {
//        String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
//        teamSharedEmailPrivateCommentService.createComment(uid, createDto);
//        return ResponseEntity.status(201).build();
//    }
//
//    @PostMapping("/public-key")
//    public ResponseEntity<Void> saveUserPublicKey(HttpServletRequest httpServletRequest,
//                                                  @RequestParam("publicKey") String publicKey) {
//        String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
//        teamSharedEmailPrivateCommentService.saveUserPublicKey(uid, publicKey);
//        return ResponseEntity.status(201).build();
//    }
//
//    @GetMapping("/shared-email/{sharedEmailId}/public-key")
//    public ResponseEntity<List<UserPublicKeyDto>> getPublicKeysForSharedEmail(@PathVariable("sharedEmailId") String sharedEmailId) {
//        List<UserPublicKeyDto> publicKeys = teamSharedEmailPrivateCommentService.getPublicKeysForSharedEmail(sharedEmailId);
//        return ResponseEntity.ok(publicKeys);
//    }
}
