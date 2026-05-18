package com.cosmoscan.analysis.repository;

import com.cosmoscan.analysis.domain.WorkWordCloud;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WorkWordCloudRepository extends JpaRepository<WorkWordCloud, UUID> {

    Optional<WorkWordCloud> findByWorkId(UUID workId);
}
