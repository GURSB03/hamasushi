package com.hamasushi.hamasushi.repository;

import com.hamasushi.hamasushi.domain.Category;
import com.hamasushi.hamasushi.domain.Sentence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SentenceRepository extends JpaRepository<Sentence, Long> {
    List<Sentence> findByCategory(Category category);
}
