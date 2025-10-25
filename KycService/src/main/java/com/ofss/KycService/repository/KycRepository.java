package com.ofss.KycService.repository;

import com.ofss.KycService.model.KycDoc;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KycRepository extends JpaRepository<KycDoc,Long> {
    Optional<KycDoc> findByCustomerId(Long customerId);
}
