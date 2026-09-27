package com.hamasushi.hamasushi.repository;

import com.hamasushi.hamasushi.domain.QuizAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizAnswerRepository extends JpaRepository<QuizAnswer, Long> {
}
