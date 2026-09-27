package com.hamasushi.hamasushi.repository;

import com.hamasushi.hamasushi.domain.Progress;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgressRepository extends JpaRepository<Progress, Long> {
}
