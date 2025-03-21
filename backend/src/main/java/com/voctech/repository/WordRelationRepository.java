package com.voctech.repository;

import com.voctech.model.WordRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WordRelationRepository
  extends JpaRepository<WordRelation, Long> {}
