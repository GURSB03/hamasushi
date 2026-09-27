package com.hamasushi.hamasushi.repository;

import com.hamasushi.hamasushi.domain.Users;
import com.hamasushi.hamasushi.domain.Word;
import com.hamasushi.hamasushi.domain.WordBookmark;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WordBookmarkRepository extends JpaRepository<WordBookmark, Long> {
    boolean existsByUsersAndWord(Users users, Word word);

}
