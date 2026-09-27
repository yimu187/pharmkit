package com.pharmkit.repository;

import com.pharmkit.entity.AppUser;
import com.pharmkit.entity.Medicine;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    @EntityGraph(attributePaths = "doses")
    List<Medicine> findAllByOwnerOrderByExpiresOnAsc(AppUser owner);

    @EntityGraph(attributePaths = "doses")
    Optional<Medicine> findWithDosesByIdAndOwner(Long id, AppUser owner);
}
