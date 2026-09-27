package com.pharmkit.config;

import com.pharmkit.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
class GoogleLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserService users;
    private final String frontendUrl;

    GoogleLoginSuccessHandler(UserService users, @Value("${app.frontend-url}") String frontendUrl) {
        this.users = users;
        this.frontendUrl = frontendUrl;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        if (authentication.getPrincipal() instanceof OAuth2User oauth) {
            users.upsertFromGoogle(oauth);
        }
        response.sendRedirect(frontendUrl);
    }
}
