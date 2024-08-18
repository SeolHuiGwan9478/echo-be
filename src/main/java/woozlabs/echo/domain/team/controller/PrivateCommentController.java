package woozlabs.echo.domain.team.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import woozlabs.echo.domain.team.dto.PrivateCommentCreateDto;
import woozlabs.echo.domain.team.dto.PrivateCommentResponseDto;
import woozlabs.echo.domain.team.service.PrivateCommentService;
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
}
