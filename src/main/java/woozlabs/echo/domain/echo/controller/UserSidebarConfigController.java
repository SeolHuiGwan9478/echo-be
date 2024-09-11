package woozlabs.echo.domain.echo.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import woozlabs.echo.domain.echo.dto.userSideBar.SidebarNavAccountDto;
import woozlabs.echo.domain.echo.service.UserSideBarConfigService;
import woozlabs.echo.global.constant.GlobalConstant;

import java.util.List;

@RestController
@RequestMapping("/api/v1/echo")
@RequiredArgsConstructor
public class UserSidebarConfigController {

    private final UserSideBarConfigService userSideBarConfigService;

//    @GetMapping("/sidebar")
//    public ResponseEntity<SidebarNavAccountDto> getConfig(HttpServletRequest httpServletRequest) {
//        String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
//        SidebarNavAccountDto sidebarNavAccount = userSideBarConfigService.getAccountsNavSpace(uid);
//        return ResponseEntity.ok(sidebarNavAccount);
//    }
//
//    @GetMapping("/sidebar/all")
//    public ResponseEntity<List<SidebarNavAccountDto>> getAllAccountsNavSpace(HttpServletRequest httpServletRequest) {
//        String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
//        List<SidebarNavAccountDto> sidebarNavAccounts = userSideBarConfigService.getAllAccountsNavSpace(uid);
//        return ResponseEntity.ok(sidebarNavAccounts);
//    }
//
//    @PostMapping("/sidebar")
//    public ResponseEntity<Void> saveConfig(HttpServletRequest httpServletRequest, @RequestBody List<SidebarNavAccountDto> dtos) {
//        String uid = (String) httpServletRequest.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
//        userSideBarConfigService.saveConfig(uid, dtos);
//        return ResponseEntity.ok().build();
//    }
}
