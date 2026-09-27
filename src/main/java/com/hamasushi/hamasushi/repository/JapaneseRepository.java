package com.hamasushi.hamasushi.repository;

import com.hamasushi.hamasushi.domain.common.Japanese;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JapaneseRepository extends JpaRepository<Japanese, Long> {
    List<Japanese> findByJpn(String jpn);
}
