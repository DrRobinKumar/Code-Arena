package com.codearena.repository;

import com.codearena.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    List<Tag> findByNameIgnoreCaseIn(List<String> names);
}
