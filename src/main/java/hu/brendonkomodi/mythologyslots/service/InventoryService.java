package hu.brendonkomodi.mythologyslots.service;

import hu.brendonkomodi.mythologyslots.domain.AppUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class InventoryService {

    private final AppUserService appUserService;

    public InventoryService(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    public Long getBalance(String username) {
        log.info("Balance requested for user: {}", username);
        AppUser appUser = appUserService.findByUsername(username);
        if (appUser.getCoinBalance() < 100) {
            appUser.setCoinBalance(20000L);
            appUserService.save(appUser);
            log.info("Balance refilled for user: {}", username);
        }
        return appUser.getCoinBalance();
    }

    public AppUser claimDailyCoins(String username) {
        log.info("Daily coin claim requested for user: {}", username);
        AppUser appUser = appUserService.findByUsername(username);
        if (appUser.getLastDailyClaimDate() != null &&
                appUser.getLastDailyClaimDate().equals(java.time.LocalDate.now())) {
            throw new RuntimeException("Daily coins already claimed today");
        }
        appUser.setCoinBalance(appUser.getCoinBalance() + 500L);
        appUser.setLastDailyClaimDate(java.time.LocalDate.now());
        return appUserService.save(appUser);
    }
}