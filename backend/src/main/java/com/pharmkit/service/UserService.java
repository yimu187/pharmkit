package com.pharmkit.service;

import com.pharmkit.entity.AppUser;
import com.pharmkit.repository.UserRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository users;

    public UserService(UserRepository users) {
        this.users = users;
    }

    @Transactional
    public AppUser upsertFromGoogle(OAuth2User oauth) {
        String googleId = stringAttr(oauth, "sub");
        String email = stringAttr(oauth, "email");
        String givenName = firstNonBlank(stringAttr(oauth, "given_name"), givenFromFullName(oauth));
        String familyName = firstNonBlank(stringAttr(oauth, "family_name"), "");
        if (googleId.isBlank() || email.isBlank()) {
            throw new IllegalStateException("Google hesabından e-posta alınamadı");
        }
        AppUser user = users.findByGoogleId(googleId).orElseGet(() -> new AppUser(googleId, givenName, familyName, email));
        user.applyProfile(givenName, familyName, email);
        return users.save(user);
    }

    @Transactional(readOnly = true)
    public AppUser requireCurrent() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new IllegalStateException("Giriş yapılmamış");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof OAuth2User oauth) {
            String googleId = stringAttr(oauth, "sub");
            return users.findByGoogleId(googleId).orElseThrow(() -> new IllegalStateException("Kullanıcı bulunamadı"));
        }
        return users.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Kullanıcı bulunamadı"));
    }

    public AppUser ensureLocalUser(String googleId, String givenName, String familyName, String email) {
        return users.findByEmail(email).orElseGet(() -> users.save(new AppUser(googleId, givenName, familyName, email)));
    }

    public AppUser save(AppUser user) {
        return users.save(user);
    }

    private static String stringAttr(OAuth2User oauth, String key) {
        Object value = oauth.getAttribute(key);
        return value == null ? "" : value.toString().trim();
    }

    private static String givenFromFullName(OAuth2User oauth) {
        String name = stringAttr(oauth, "name");
        if (name.isBlank()) {
            return "Kullanıcı";
        }
        return name.split("\\s+")[0];
    }

    private static String firstNonBlank(String first, String second) {
        return first.isBlank() ? second : first;
    }
}
