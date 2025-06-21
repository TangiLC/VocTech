package com.voctech.service;

import com.voctech.model.Theme;
import com.voctech.model.Word;
import com.voctech.model.WordRelation;
import com.voctech.payload.RelatedWordResponse;
import com.voctech.payload.UpdateWordRequest;
import com.voctech.payload.WordResponse;
import com.voctech.repository.WordRepository;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WordService {

  private final WordRepository wordRepository;
  private final ThemeService themeService;

  public WordService(WordRepository wordRepository, ThemeService themeService) {
    this.wordRepository = wordRepository;
    this.themeService = themeService;
  }

  /**
   * Recherche des mots contenant une séquence de lettres donnée,
   * sans distinction de casse et d'accentuation.
   *
   * @param word Le mot recherché
   * @return Liste des mots correspondant au critère de recherche
   */
  public List<WordResponse> searchWords(String word) {
    String normalizedWord = normalize(word);
    return wordRepository
      .findByWordContainingIgnoreCase(normalizedWord)
      .stream()
      .map(this::mapWordToResponse)
      .collect(Collectors.toList());
  }

  private WordResponse mapWordToResponse(Word w) {
    Set<Long> visitedWordIds = new HashSet<>();
    return mapWordToResponseWithVisited(w, visitedWordIds);
  }

  private WordResponse mapWordToResponseWithVisited(
    Word w,
    Set<Long> visitedWordIds
  ) {
    visitedWordIds.add(w.getId());

    List<Integer> themeIds = w
      .getThemes()
      .stream()
      .map(Theme::getId)
      .collect(Collectors.toList());

    Map<String, List<RelatedWordResponse>> relations = new HashMap<>();
    relations.put("translation", new ArrayList<>());
    relations.put("synonym", new ArrayList<>());
    relations.put("antonym", new ArrayList<>());

    if (w.getSourceRelations() != null) {
      for (WordRelation relation : w.getSourceRelations()) {
        Word targetWord = relation.getWordTarget();
        if (targetWord != null) {
          String relationType = relation.getType().name().toLowerCase();

          if (
            "translation".equals(relationType) || "synonym".equals(relationType)
          ) {
            if (!w.getLanguage().equals(targetWord.getLanguage())) {
              relationType = "translation";
            } else { // V2 : antonym ?
              relationType = "synonym";
            }
          }

          if (relations.containsKey(relationType)) {
            relations
              .get(relationType)
              .add(
                new RelatedWordResponse(
                  targetWord.getId(),
                  targetWord.getWord(),
                  targetWord.getLanguage()
                )
              );

            // Récursivité pour les mots non visités
            if (!visitedWordIds.contains(targetWord.getId())) {
              Set<Long> newVisitedIds = new HashSet<>(visitedWordIds);
              WordResponse targetResponse = mapWordToResponseWithVisited(
                targetWord,
                newVisitedIds
              );

              if ("translation".equals(relationType)) {
                for (RelatedWordResponse transOfTrans : targetResponse
                  .getRelations()
                  .get("translation")) {
                  if (!transOfTrans.getLanguage().equals(w.getLanguage())) {
                    relations.get("translation").add(transOfTrans);
                  }
                }

                for (RelatedWordResponse synOfTrans : targetResponse
                  .getRelations()
                  .get("synonym")) {
                  if (!synOfTrans.getLanguage().equals(w.getLanguage())) {
                    relations.get("translation").add(synOfTrans);
                  } else {
                    relations.get("synonym").add(synOfTrans);
                  }
                }
              }

              if ("synonym".equals(relationType)) {
                relations
                  .get("synonym")
                  .addAll(targetResponse.getRelations().get("synonym"));

                relations
                  .get("translation")
                  .addAll(targetResponse.getRelations().get("translation"));
              }
            }
          }
        }
      }
    }

    if (w.getTargetRelations() != null) {
      for (WordRelation relation : w.getTargetRelations()) {
        Word sourceWord = relation.getWordSource();
        if (sourceWord != null) {
          String relationType = relation.getType().name().toLowerCase();

          if (
            "translation".equals(relationType) || "synonym".equals(relationType)
          ) {
            if (!w.getLanguage().equals(sourceWord.getLanguage())) {
              relationType = "translation";
            } else {
              relationType = "synonym";
            }
          }

          if (relations.containsKey(relationType)) {
            relations
              .get(relationType)
              .add(
                new RelatedWordResponse(
                  sourceWord.getId(),
                  sourceWord.getWord(),
                  sourceWord.getLanguage()
                )
              );

            if (!visitedWordIds.contains(sourceWord.getId())) {
              Set<Long> newVisitedIds = new HashSet<>(visitedWordIds);
              WordResponse sourceResponse = mapWordToResponseWithVisited(
                sourceWord,
                newVisitedIds
              );

              if ("translation".equals(relationType)) {
                for (RelatedWordResponse transOfTrans : sourceResponse
                  .getRelations()
                  .get("translation")) {
                  if (!transOfTrans.getLanguage().equals(w.getLanguage())) {
                    relations.get("translation").add(transOfTrans);
                  }
                }

                for (RelatedWordResponse synOfTrans : sourceResponse
                  .getRelations()
                  .get("synonym")) {
                  if (!synOfTrans.getLanguage().equals(w.getLanguage())) {
                    relations.get("translation").add(synOfTrans);
                  } else {
                    relations.get("synonym").add(synOfTrans);
                  }
                }
              }

              if ("synonym".equals(relationType)) {
                relations
                  .get("synonym")
                  .addAll(sourceResponse.getRelations().get("synonym"));

                relations
                  .get("translation")
                  .addAll(sourceResponse.getRelations().get("translation"));
              }
            }
          }
        }
      }
    }

    // Éliminer les doublons dans chaque liste de relations
    for (String key : relations.keySet()) {
      List<RelatedWordResponse> uniqueList = new ArrayList<>();
      Set<Long> addedIds = new HashSet<>();

      for (RelatedWordResponse resp : relations.get(key)) {
        if (
          !addedIds.contains(resp.getId()) && !resp.getId().equals(w.getId())
        ) {
          uniqueList.add(resp);
          addedIds.add(resp.getId());
        }
      }

      relations.put(key, uniqueList);
    }

    return new WordResponse(
      w.getId(),
      w.getWord(),
      w.getLanguage(),
      themeIds,
      relations
    );
  }

/**
   * Récupère les n derniers mots selon leur ID (ordre décroissant).
   * Si n=0, retourne tous les mots.
   *
   * @param n Le nombre de mots à récupérer (0 pour tous)
   * @return Liste des n derniers mots, sous forme de WordResponse
   */
  public List<WordResponse> getLastNWords(int n) {
    if (n == 0) {
      return getAllWords();
    }
    
    List<Word> words = wordRepository.findLastNWords(n);
    return words.stream()
      .limit(n)
      .map(this::mapWordToResponse)
      .collect(Collectors.toList());
  }


  /**
   * Récupère tous les mots dans la base de données.
   *
   * @return Liste de tous les mots, sous forme de WordResponse
   */
  public List<WordResponse> getAllWords() {
    return wordRepository
      .findAll()
      .stream()
      .map(this::mapWordToResponse)
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
      Set<Theme> updatedThemes = themeService.getThemesByIds(
        request.getThemeId()
      );
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
      return ResponseEntity
        .badRequest()
        .body("{\"error\": \"ID du mot inexistant\"}");
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
}
