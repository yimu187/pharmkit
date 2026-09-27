package com.pharmkit.repository;

import com.pharmkit.entity.Dose;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DoseRepository extends JpaRepository<Dose, Long> {

    List<Dose> findByMedicineIdOrderByTakenAtDesc(Long medicineId);
}
