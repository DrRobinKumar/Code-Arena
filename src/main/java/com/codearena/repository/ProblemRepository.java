package com.codearena.repository;

import com.codearena.entity.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

/**
 * JpaSpecificationExecutor lets ProblemServiceImpl compose search/difficulty/
 * tag filters independently (via ProblemSpecification) instead of one
 * @Query with a growing list of optional parameters.
 */
public interface ProblemRepository extends JpaRepository<Problem, Long>, JpaSpecificationExecutor<Problem> {

    Optional<Problem> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
