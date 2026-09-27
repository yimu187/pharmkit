package com.pharmkit.dto;

import java.time.LocalDateTime;

public record DoseRequest(LocalDateTime takenAt) {
}
