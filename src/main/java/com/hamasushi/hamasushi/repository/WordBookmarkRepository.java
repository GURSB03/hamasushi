package com.hamasushi.hamasushi.repository;

import com.hamasushi.hamasushi.domain.Sentence;
import com.hamasushi.hamasushi.domain.Users;
import com.hamasushi.hamasushi.domain.WordBookmark;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WordBookmarkRepository extends JpaRepository<WordBookmark, Long> {
    boolean existsByUsersAndSentence(Users users, Sentence sentence);

}
