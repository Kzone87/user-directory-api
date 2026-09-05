package dev.kzone.portfolio.userapi.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @GetMapping("/me")
    public AuthMeResponse me(Authentication authentication) {
        List<String> roles = authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority().replaceFirst("^ROLE_", ""))
                .sorted()
                .toList();
        return new AuthMeResponse(authentication.getName(), roles);
    }

    public record AuthMeResponse(String username, List<String> roles) {
    }
}
