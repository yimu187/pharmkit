package com.pharmkit.dto;

import com.pharmkit.entity.Dose;

import java.time.LocalDateTime;

public record DoseResponse(Long id, Long medicineId, LocalDateTime takenAt) {

    public static DoseResponse from(Dose dose) {
        return new DoseResponse(dose.getId(), dose.getMedicine().getId(), dose.getTakenAt());
    }
}
