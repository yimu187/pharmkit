package com.pharmkit.dto;

import com.pharmkit.entity.AppUser;

public record UserResponse(String givenName, String familyName, String email) {

    public static UserResponse from(AppUser user) {
        return new UserResponse(user.getGivenName(), user.getFamilyName(), user.getEmail());
    }
}
