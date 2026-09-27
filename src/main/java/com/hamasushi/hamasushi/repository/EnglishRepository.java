package com.hamasushi.hamasushi.repository;

import com.hamasushi.hamasushi.domain.common.English;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnglishRepository extends JpaRepository<English, Long> {
    List<English> findByEng(String eng);
}
