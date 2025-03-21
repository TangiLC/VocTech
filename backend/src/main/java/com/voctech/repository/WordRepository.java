package com.voctech.repository;

import com.voctech.model.Word;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface WordRepository extends JpaRepository<Word, Long> {
  @Query(
    "SELECT w FROM Word w WHERE LOWER(w.word) LIKE LOWER(CONCAT('%', :word, '%'))"
  )
  List<Word> findByWordContainingIgnoreCase(String word);

  Optional<Word> findByWordIgnoreCase(String word);
}
