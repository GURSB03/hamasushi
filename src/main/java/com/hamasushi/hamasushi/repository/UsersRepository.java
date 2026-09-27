package com.hamasushi.hamasushi.repository;

import com.hamasushi.hamasushi.domain.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersRepository extends JpaRepository<Users, Long> {
}
