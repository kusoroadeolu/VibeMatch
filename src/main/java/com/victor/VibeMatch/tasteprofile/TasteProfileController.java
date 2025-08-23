package com.victor.VibeMatch.tasteprofile;

import com.victor.VibeMatch.misc.UUIDValidator;
import com.victor.VibeMatch.security.UserPrincipal;
import com.victor.VibeMatch.tasteprofile.dto.TasteProfileResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class TasteProfileController {
    private final TasteProfileService tasteProfileService;
    private final UUIDValidator uuidValidator;

    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<TasteProfileResponseDto> getTasteProfile(@AuthenticationPrincipal UserPrincipal userPrincipal){
        UUID userId = userPrincipal.getId();
        var response = tasteProfileService.getTasteProfile(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<TasteProfileResponseDto> getPublicUserTasteProfile(@AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable("id") String id){
       UUID targetUserId = uuidValidator.handleUUID(id);

       var response = tasteProfileService.getTasteProfile(targetUserId);
       return ResponseEntity.ok(response);
    }

    @DeleteMapping("/me")
    @PreAuthorize("hasRole('USER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteTasteProfile(@AuthenticationPrincipal UserPrincipal userPrincipal){
        tasteProfileService.removeTasteProfile(userPrincipal.getId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
