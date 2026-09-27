package com.hamasushi.hamasushi.repository;

import com.hamasushi.hamasushi.domain.Sentence;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SentenceRepository extends JpaRepository<Sentence, Long> {
}
