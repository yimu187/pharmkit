package com.pharmkit.exception;

public class DoseNotFoundException extends RuntimeException {

    public DoseNotFoundException(Long id) {
        super("Doz kaydı bulunamadı: " + id);
    }
}
