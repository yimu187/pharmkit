package com.pharmkit.controller;

import com.pharmkit.dto.DoseRequest;
import com.pharmkit.dto.DoseResponse;
import com.pharmkit.dto.MedicineRequest;
import com.pharmkit.dto.MedicineResponse;
import com.pharmkit.service.DoseService;
import com.pharmkit.service.MedicineService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/medicines")
class MedicineController {

    private final MedicineService medicines;
    private final DoseService doses;

    MedicineController(MedicineService medicines, DoseService doses) {
        this.medicines = medicines;
        this.doses = doses;
    }

    @GetMapping
    List<MedicineResponse> list() {
        return medicines.findAll();
    }

    @GetMapping("/{id}")
    MedicineResponse get(@PathVariable Long id) {
        return medicines.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    MedicineResponse create(@Valid @RequestBody MedicineRequest request) {
        return medicines.create(request);
    }

    @PutMapping("/{id}")
    MedicineResponse update(@PathVariable Long id, @Valid @RequestBody MedicineRequest request) {
        return medicines.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable Long id) {
        medicines.delete(id);
    }

    @GetMapping("/{id}/doses")
    List<DoseResponse> listDoses(@PathVariable Long id) {
        return doses.findByMedicine(id);
    }

    @PostMapping("/{id}/doses")
    @ResponseStatus(HttpStatus.CREATED)
    DoseResponse addDose(@PathVariable Long id, @RequestBody(required = false) DoseRequest request) {
        return doses.create(id, request);
    }
}
