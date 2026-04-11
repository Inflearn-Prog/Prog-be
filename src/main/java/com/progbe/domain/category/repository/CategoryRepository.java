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

    @Query("SELECT c FROM CategoryEntity c WHERE c.deletedAt IS NULL ORDER BY c.displayOrder ASC")
    List<CategoryEntity> findAllByNotDeleted();

    @Query("SELECT COUNT(c) > 0 FROM CategoryEntity c WHERE c.parent.id = :parentId AND c.deletedAt IS NULL")
    boolean existsChildrenByParentId(@Param("parentId") Long parentId);

    @Query("SELECT c FROM CategoryEntity c WHERE c.name = :name AND c.deletedAt IS NULL")
    Optional<CategoryEntity> findByNameAndNotDeleted(@Param("name") String name);

    @Query("SELECT c FROM CategoryEntity c WHERE c.id IN :ids AND c.deletedAt IS NULL")
    List<CategoryEntity> findAllByIdsAndNotDeleted(@Param("ids") List<Long> ids);

    @Query("SELECT COALESCE(MAX(c.displayOrder), -1) FROM CategoryEntity c WHERE c.deletedAt IS NULL")
    int findMaxDisplayOrder();
}

