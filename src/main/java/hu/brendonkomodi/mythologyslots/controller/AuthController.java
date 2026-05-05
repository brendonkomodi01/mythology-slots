package hu.brendonkomodi.mythologyslots.controller;

import hu.brendonkomodi.mythologyslots.domain.AppUser;
import hu.brendonkomodi.mythologyslots.security.JwtUtil;
import hu.brendonkomodi.mythologyslots.service.AppUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AppUserService appUserService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthController(AppUserService appUserService,
                          JwtUtil jwtUtil,
                          AuthenticationManager authenticationManager) {
        this.appUserService = appUserService;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register")
    public ResponseEntity<AppUser> register(@RequestParam String username,
                                            @RequestParam String password) {
        log.info("Register request for username: {}", username);
        AppUser appUser = appUserService.register(username, password);
        return ResponseEntity.ok(appUser);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam String username,
                                        @RequestParam String password) {
        log.info("Login request for username: {}", username);
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));
        String token = jwtUtil.generateToken(username);
        return ResponseEntity.ok(token);
    }
}