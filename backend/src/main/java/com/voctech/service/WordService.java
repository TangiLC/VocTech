package com.voctech.service;

import com.voctech.model.Theme;
import com.voctech.model.Word;
import com.voctech.payload.UpdateWordRequest;
import com.voctech.payload.WordResponse;
import com.voctech.repository.ThemeRepository;
import com.voctech.repository.WordRepository;
import java.text.Normalizer;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WordService {

  private final WordRepository wordRepository;
  private final ThemeRepository themeRepository;

  public WordService(
    WordRepository wordRepository,
    ThemeRepository themeRepository
  ) {
    this.wordRepository = wordRepository;
    this.themeRepository = themeRepository;
  }

  /**
   * Recherche des mots contenant une séquence de lettres donnée, sans distinction de casse et d'accentuation.
   *
   * @param word Le mot recherché
   * @return Liste des mots correspondant au critère de recherche
   */
  public List<WordResponse> searchWords(String word) {
    String normalizedWord = normalize(word);
    return wordRepository
      .findByWordContainingIgnoreCase(normalizedWord)
      .stream()
      .map(w ->
        new WordResponse(
          w.getId(),
          w.getWord(),
          w.getLanguage(),
          w.getThemes().stream().map(Theme::getId).collect(Collectors.toList())
        )
      )
      .collect(Collectors.toList());
  }

  /**
   * Met à jour un mot existant dans la base de données.
   *
   * @param request Contient l'ID du mot à modifier, ainsi que les nouvelles valeurs (word, language, themeId).
   * @return ResponseEntity avec un message de succès ou une erreur si l'ID n'existe pas.
   */
  @Transactional
  public ResponseEntity<?> updateWord(UpdateWordRequest request) {
    Optional<Word> existingWordOpt = wordRepository.findById(
      request.getId().longValue()
    );
    if (existingWordOpt.isEmpty()) {
      return ResponseEntity
        .badRequest()
        .body("{\"error\": \"ID du mot inexistant\"}");
    }

    Word existingWord = existingWordOpt.get();

    if (request.getWord() != null && !request.getWord().isBlank()) {
      existingWord.setWord(request.getWord());
    }

    if (request.getLanguage() != null && !request.getLanguage().isBlank()) {
      existingWord.setLanguage(request.getLanguage());
    }

    if (request.getThemeId() != null && !request.getThemeId().isEmpty()) {
      Set<Theme> updatedThemes = getThemesByIds(request.getThemeId());
      existingWord.setThemes(updatedThemes);
    }

    wordRepository.save(existingWord);
    return ResponseEntity.ok("{\"message\": \"Mot mis à jour avec succès\"}");
  }

  /**
     * Supprime un mot et ses relations en cascade.
     *
     * @param id ID du mot à supprimer
     * @return ResponseEntity avec un message de succès ou une erreur si l'ID est inexistant.
     */
    @Transactional
    public ResponseEntity<?> deleteWord(Long id) {
        Optional<Word> existingWordOpt = wordRepository.findById(id);
        if (existingWordOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("{\"error\": \"ID du mot inexistant\"}");
        }
        
        wordRepository.delete(existingWordOpt.get());
        return ResponseEntity.ok("{\"message\": \"Mot supprimé avec succès\"}");
    }


  /**
   * Normalise une chaîne de caractères en supprimant les accents et en mettant en minuscules.
   *
   * @param input La chaîne de caractères à normaliser
   * @return La chaîne normalisée
   */
  private String normalize(String input) {
    return Normalizer
      .normalize(input, Normalizer.Form.NFD)
      .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
      .toLowerCase();
  }

  /**
   * Récupère un ensemble de thèmes à partir d'une liste d'ID.
   *
   * @param themeIds Liste des ID des thèmes
   * @return Un ensemble d'objets Theme correspondant aux ID fournis
   */
  private Set<Theme> getThemesByIds(List<Integer> themeIds) {
    Set<Theme> themes = new HashSet<>();
    if (themeIds != null) {
      themeIds.forEach(id ->
        themeRepository.findById(id).ifPresent(themes::add)
      );
    }
    return themes;
  }
}
