package com.hamasushi.hamasushi.repository;

import com.hamasushi.hamasushi.domain.Category;
import com.hamasushi.hamasushi.domain.Word;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WordRepository extends JpaRepository<Word, Long> {
    List<Word> findByCategory(Category category);
}
