package com.voctech.service;

import com.voctech.model.Theme;
import com.voctech.payload.ThemeResponse;
import com.voctech.repository.ThemeRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service pour la gestion des thèmes.
 */
@Service
public class ThemeService {

  private final ThemeRepository themeRepository;

  /**
   * Constructeur du service ThemeService.
   *
   * @param themeRepository Repository pour la gestion des thèmes
   */
  public ThemeService(ThemeRepository themeRepository) {
    this.themeRepository = themeRepository;
  }

  /**
   * Récupère tous les thèmes stockés dans la base de données.
   *
   * @return Liste des thèmes
   */
  @Transactional(readOnly = true)
  public List<ThemeResponse> getAllThemes() {
    return themeRepository.findAllThemesWithoutWords();
  }


  /**
   * Récupère un ensemble de thèmes à partir d'une liste d'ID.
   *
   * @param themeIds Liste des ID des thèmes
   * @return Un ensemble d'objets Theme correspondant aux ID fournis
   */
  public Set<Theme> getThemesByIds(List<Integer> themeIds) {
    Set<Theme> themes = new HashSet<>();
    if (themeIds != null) {
      themeIds.forEach(id -> themeRepository.findById(id).ifPresent(themes::add)
      );
    }
    return themes;
  }
}
