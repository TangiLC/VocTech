package com.voctech.repository;

import com.voctech.model.Theme;
import com.voctech.payload.ThemeResponse;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ThemeRepository extends JpaRepository<Theme, Integer> {
  @Query(
    "SELECT new com.voctech.payload.ThemeResponse(t.id, t.nameFr, t.nameEn, t.descFr, t.descEn) FROM Theme t"
  )
  List<ThemeResponse> findAllThemesWithoutWords();
}
