package com.cosmoscan.analysis.repository;

import com.cosmoscan.analysis.domain.TechnicalReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TechnicalReportRepository extends JpaRepository<TechnicalReport, UUID> {

    List<TechnicalReport> findByWorkIdOrderByCheckedAtDesc(UUID workId);
}
