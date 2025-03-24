package com.voctech.service;

import com.voctech.model.Theme;
import com.voctech.payload.ThemeResponse;
import com.voctech.repository.ThemeRepository;
import java.util.List;
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
}
