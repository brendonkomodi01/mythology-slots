package hu.brendonkomodi.mythologyslots.controller;

import hu.brendonkomodi.mythologyslots.service.InventoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/balance")
    public ResponseEntity<Long> getBalance(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Balance requested for user: {}", userDetails.getUsername());
        return ResponseEntity.ok(inventoryService.getBalance(userDetails.getUsername()));
    }

    @PostMapping("/daily")
    public ResponseEntity<?> claimDailyCoins(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Daily claim requested for user: {}", userDetails.getUsername());
        return ResponseEntity.ok(inventoryService.claimDailyCoins(userDetails.getUsername()));
    }
}