package com.pharmkit.service;

import com.pharmkit.dto.MedicineRequest;
import com.pharmkit.dto.MedicineResponse;
import com.pharmkit.entity.AppUser;
import com.pharmkit.entity.Medicine;
import com.pharmkit.exception.InvalidMedicineException;
import com.pharmkit.exception.MedicineNotFoundException;
import com.pharmkit.repository.MedicineRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MedicineService {

    private final MedicineRepository medicines;
    private final UserService users;

    public MedicineService(MedicineRepository medicines, UserService users) {
        this.medicines = medicines;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public List<MedicineResponse> findAll() {
        AppUser owner = users.requireCurrent();
        return medicines.findAllByOwnerOrderByExpiresOnAsc(owner).stream()
                .map(MedicineResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public MedicineResponse findById(Long id) {
        return MedicineResponse.from(require(id));
    }

    @Transactional
    public MedicineResponse create(MedicineRequest request) {
        validateDates(request);
        AppUser owner = users.requireCurrent();
        Medicine medicine = new Medicine(
                owner,
                request.name().trim(),
                request.purchasedOn(),
                request.expiresOn(),
                request.intakeSchedule().trim(),
                request.homeLocation().trim()
        );
        return MedicineResponse.from(medicines.save(medicine));
    }

    @Transactional
    public MedicineResponse update(Long id, MedicineRequest request) {
        validateDates(request);
        Medicine medicine = require(id);
        medicine.apply(
                request.name().trim(),
                request.purchasedOn(),
                request.expiresOn(),
                request.intakeSchedule().trim(),
                request.homeLocation().trim()
        );
        return MedicineResponse.from(medicine);
    }

    @Transactional
    public void delete(Long id) {
        medicines.delete(require(id));
    }

    private Medicine require(Long id) {
        return medicines.findWithDosesByIdAndOwner(id, users.requireCurrent())
                .orElseThrow(() -> new MedicineNotFoundException(id));
    }

    private void validateDates(MedicineRequest request) {
        if (request.expiresOn().isBefore(request.purchasedOn())) {
            throw new InvalidMedicineException("Son kullanma tarihi, alış tarihinden önce olamaz");
        }
    }
}
