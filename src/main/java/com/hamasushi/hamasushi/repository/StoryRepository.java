package com.hamasushi.hamasushi.repository;

import com.hamasushi.hamasushi.domain.Story;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoryRepository extends JpaRepository<Story, Long> {
}
