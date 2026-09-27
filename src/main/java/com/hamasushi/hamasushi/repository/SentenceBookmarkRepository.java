package com.hamasushi.hamasushi.repository;

import com.hamasushi.hamasushi.domain.Sentence;
import com.hamasushi.hamasushi.domain.SentenceBookmark;
import com.hamasushi.hamasushi.domain.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SentenceBookmarkRepository extends JpaRepository<SentenceBookmark, Long> {
    boolean existsByUsersAndSentence(Users users, Sentence sentence);
}
