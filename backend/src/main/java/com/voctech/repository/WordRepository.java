package com.voctech.repository;

import com.voctech.model.Word;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WordRepository extends JpaRepository<Word, Long> {
  @Query(
    //"SELECT w FROM Word w WHERE LOWER(w.word) LIKE LOWER(CONCAT('%', :word, '%'))"
    "SELECT w FROM Word w "+
    "LEFT JOIN FETCH w.sourceRelations sr " +
    "LEFT JOIN FETCH sr.wordTarget " +
    "WHERE LOWER(w.word) LIKE LOWER(CONCAT('%', :word, '%'))"
  )
  List<Word> findByWordContainingIgnoreCase(@Param("word") String word);

  Optional<Word> findByWordIgnoreCase(String word);

   @Query(
    "SELECT w FROM Word w " +
    "LEFT JOIN FETCH w.sourceRelations sr " +
    "LEFT JOIN FETCH sr.wordTarget " +
    "ORDER BY w.id DESC"
  )
  List<Word> findLastNWords(@Param("limit") int limit);
}
