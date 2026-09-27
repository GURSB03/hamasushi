package com.hamasushi.hamasushi.repository;

import com.hamasushi.hamasushi.domain.Theme;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ThemeRepository extends JpaRepository<Theme, Long> {
}
