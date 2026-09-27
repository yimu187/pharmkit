package com.pharmkit.service;

import com.pharmkit.dto.DoseRequest;
import com.pharmkit.dto.DoseResponse;
import com.pharmkit.entity.Dose;
import com.pharmkit.entity.Medicine;
import com.pharmkit.exception.DoseNotFoundException;
import com.pharmkit.exception.MedicineNotFoundException;
import com.pharmkit.repository.DoseRepository;
import com.pharmkit.repository.MedicineRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DoseService {

    private final MedicineRepository medicines;
    private final DoseRepository doses;
    private final UserService users;

    public DoseService(MedicineRepository medicines, DoseRepository doses, UserService users) {
        this.medicines = medicines;
        this.doses = doses;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public List<DoseResponse> findByMedicine(Long medicineId) {
        requireMedicine(medicineId);
        return doses.findByMedicineIdOrderByTakenAtDesc(medicineId).stream()
                .map(DoseResponse::from)
                .toList();
    }

    @Transactional
    public DoseResponse create(Long medicineId, DoseRequest request) {
        Medicine medicine = requireMedicine(medicineId);
        LocalDateTime takenAt = request == null || request.takenAt() == null
                ? LocalDateTime.now()
                : request.takenAt();
        Dose dose = doses.save(new Dose(medicine, takenAt));
        return DoseResponse.from(dose);
    }

    @Transactional
    public void delete(Long id) {
        Dose dose = doses.findById(id).orElseThrow(() -> new DoseNotFoundException(id));
        requireMedicine(dose.getMedicine().getId());
        doses.delete(dose);
    }

    private Medicine requireMedicine(Long medicineId) {
        return medicines.findWithDosesByIdAndOwner(medicineId, users.requireCurrent())
                .orElseThrow(() -> new MedicineNotFoundException(medicineId));
    }
}
