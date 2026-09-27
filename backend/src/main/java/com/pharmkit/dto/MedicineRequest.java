package com.pharmkit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record MedicineRequest(
        @NotBlank(message = "İlaç adı zorunludur")
        String name,
        @NotNull(message = "Alış tarihi zorunludur")
        LocalDate purchasedOn,
        @NotNull(message = "Son kullanma tarihi zorunludur")
        LocalDate expiresOn,
        @NotBlank(message = "Reçete alınış sıklığı zorunludur")
        String intakeSchedule,
        @NotBlank(message = "Evdeki yer bilgisi zorunludur")
        String homeLocation
) {
}
