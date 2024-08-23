package woozlabs.echo.domain.sharedEmail.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import woozlabs.echo.domain.sharedEmail.dto.PrivateCommentCreateDto;
import woozlabs.echo.domain.sharedEmail.dto.PrivateCommentResponseDto;
import woozlabs.echo.domain.sharedEmail.dto.UserPublicKeyDto;
import woozlabs.echo.domain.sharedEmail.entity.UserPublicKey;
import woozlabs.echo.domain.sharedEmail.service.PrivateCommentService;
import woozlabs.echo.global.constant.GlobalConstant;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/echo")
public class PrivateCommentController {

    private final PrivateCommentService privateCommentService;

    @GetMapping("/shared-email/{sharedEmailId}")
    public ResponseEntity<List<PrivateCommentResponseDto>> getCommentsBySharedEmailId(HttpServletRequest httpServletRequest,
                                                                                      @PathVariable("sharedEmailId") String sharedEmailId) {
        String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
        List<PrivateCommentResponseDto> comments = privateCommentService.getCommentsBySharedEmailId(uid, sharedEmailId);
        return ResponseEntity.ok(comments);
    }

    @PostMapping("/shared-email")
    public ResponseEntity<Void> createComment(HttpServletRequest httpServletRequest,
                                              @RequestBody PrivateCommentCreateDto createDto) {
        String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
        privateCommentService.createComment(uid, createDto);
        return ResponseEntity.status(201).build();
    }

    @PostMapping("/public-key")
    public ResponseEntity<Void> saveUserPublicKey(HttpServletRequest httpServletRequest,
                                                  @RequestParam("publicKey") String publicKey) {
        String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
        privateCommentService.saveUserPublicKey(uid, publicKey);
        return ResponseEntity.status(201).build();
    }

    @GetMapping("/shared-email/{sharedEmailId}/public-key")
    public ResponseEntity<List<UserPublicKeyDto>> getPublicKeysForSharedEmail(@PathVariable("sharedEmailId") String sharedEmailId) {
        List<UserPublicKeyDto> publicKeys = privateCommentService.getPublicKeysForSharedEmail(sharedEmailId);
        return ResponseEntity.ok(publicKeys);
    }
}
