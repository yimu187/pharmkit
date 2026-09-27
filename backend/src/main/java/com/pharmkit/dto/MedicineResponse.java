package com.pharmkit.dto;

import com.pharmkit.entity.Medicine;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record MedicineResponse(
        Long id,
        String name,
        LocalDate purchasedOn,
        LocalDate expiresOn,
        String intakeSchedule,
        String homeLocation,
        LocalDateTime lastTakenAt,
        int doseCount,
        List<DoseResponse> doses
) {

    public static MedicineResponse from(Medicine medicine) {
        List<DoseResponse> doses = medicine.getDoses().stream()
                .map(DoseResponse::from)
                .toList();
        LocalDateTime lastTakenAt = doses.isEmpty() ? null : doses.getFirst().takenAt();
        return new MedicineResponse(
                medicine.getId(),
                medicine.getName(),
                medicine.getPurchasedOn(),
                medicine.getExpiresOn(),
                medicine.getIntakeSchedule(),
                medicine.getHomeLocation(),
                lastTakenAt,
                doses.size(),
                doses
        );
    }
}
