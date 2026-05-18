package com.cosmoscan.submission.repository;

import com.cosmoscan.submission.domain.Work;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WorkRepository extends JpaRepository<Work, UUID> {
}
