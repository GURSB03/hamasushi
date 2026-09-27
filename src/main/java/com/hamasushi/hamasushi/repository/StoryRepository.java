package com.hamasushi.hamasushi.repository;

import com.hamasushi.hamasushi.domain.Story;
import com.hamasushi.hamasushi.domain.Theme;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoryRepository extends JpaRepository<Story, Long> {
    List<Story> findByThemeOrderByStepOrderAsc(Theme theme);
}
