package com.hamasushi.hamasushi.repository;

import com.hamasushi.hamasushi.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

}
