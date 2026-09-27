package com.hamasushi.hamasushi.repository;

import com.hamasushi.hamasushi.domain.WordBookmark;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WordBookmarkRepository extends JpaRepository<WordBookmark, Long> {
}
