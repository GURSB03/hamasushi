package com.hamasushi.hamasushi.repository;

import com.hamasushi.hamasushi.domain.Progress;
import com.hamasushi.hamasushi.domain.Theme;
import com.hamasushi.hamasushi.domain.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProgressRepository extends JpaRepository<Progress, Long> {
    List<Progress> findByUsersAndTheme(Users users, Theme theme);
}
