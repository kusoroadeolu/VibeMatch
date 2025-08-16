package com.victor.VibeMatch.tasteprofile;

import com.victor.VibeMatch.security.UserPrincipal;
import com.victor.VibeMatch.tasteprofile.dto.TasteProfileResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class TasteProfileController {
    private final TasteProfileService tasteProfileService;

    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<TasteProfileResponseDto> getUserTasteProfile(@AuthenticationPrincipal UserPrincipal userPrincipal){
        UUID userId = userPrincipal.getId();
        var response = tasteProfileService.createTasteProfile(userId);
        return ResponseEntity.ok(response);
    }
}
