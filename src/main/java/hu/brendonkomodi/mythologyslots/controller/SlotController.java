package hu.brendonkomodi.mythologyslots.controller;

import hu.brendonkomodi.mythologyslots.dto.outgoing.SpinResultDto;
import hu.brendonkomodi.mythologyslots.service.SlotService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/slot")
public class SlotController {

    private final SlotService slotService;

    public SlotController(SlotService slotService) {
        this.slotService = slotService;
    }

    @PostMapping("/spin")
    public ResponseEntity<SpinResultDto> spin(@AuthenticationPrincipal UserDetails userDetails,
                                              @RequestParam long bet,
                                              @RequestBody(required = false) List<Map<String, Integer>> stickyWilds) {
        log.info("Spin requested by user: {} with bet: {}", userDetails.getUsername(), bet);
        if (stickyWilds == null) stickyWilds = List.of();
        return ResponseEntity.ok(slotService.spin(userDetails.getUsername(), bet, stickyWilds));
    }
}