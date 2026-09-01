package com.paulo.independentschooldata.repos;

import com.paulo.independentschooldata.domain.School;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface SchoolRepository extends JpaRepository<School, Long>, JpaSpecificationExecutor<School> {

    // Find by UUID
    Optional<School> findByUuid(UUID uuid);

    // Search by name (case-insensitive, contains)
    Page<School> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // Filter by gender profile
    Page<School> findByGenderProfileIgnoreCase(String genderProfile, Pageable pageable);

    // Filter by religious affiliation
    Page<School> findByReligiousAffiliationIgnoreCase(String religiousAffiliation, Pageable pageable);

    Page<School> findByIsTestFalseAndDeletedFalse(Pageable pageable);
    // You can keep adding more filters as needed

//    Page<School> findAll(Pageable pageable);

}
