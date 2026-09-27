package com.hamasushi.hamasushi.repository;

import com.hamasushi.hamasushi.domain.Word;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WordRepository extends JpaRepository<Word, Long> {
}
