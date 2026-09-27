package com.pharmkit.exception;

public class MedicineNotFoundException extends RuntimeException {

    public MedicineNotFoundException(Long id) {
        super("İlaç bulunamadı: " + id);
    }
}
