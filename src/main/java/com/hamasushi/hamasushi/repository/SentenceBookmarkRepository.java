package com.hamasushi.hamasushi.repository;

import com.hamasushi.hamasushi.domain.SentenceBookmark;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SentenceBookmarkRepository extends JpaRepository<SentenceBookmark, Long> {
}
