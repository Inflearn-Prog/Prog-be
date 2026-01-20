package com.progbe.domain.category.repository;

import com.progbe.domain.category.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {

    @Query("SELECT c FROM CategoryEntity c WHERE c.id = :id AND c.deletedAt IS NULL")
    Optional<CategoryEntity> findByIdAndNotDeleted(@Param("id") Long id);

    @Query("SELECT c FROM CategoryEntity c WHERE c.deletedAt IS NULL ORDER BY c.id ASC")
    List<CategoryEntity> findAllByNotDeleted();
}

