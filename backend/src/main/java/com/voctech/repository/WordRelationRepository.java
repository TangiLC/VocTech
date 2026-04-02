package com.voctech.repository;

import com.voctech.model.Word;
import com.voctech.model.WordRelation;
import com.voctech.model.WordRelation.RelationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WordRelationRepository
  extends JpaRepository<WordRelation, Long> {
  boolean existsByWordSourceAndWordTargetAndType(
    Word source,
    Word target,
    RelationType type
  );
}
