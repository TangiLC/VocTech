package com.voctech.service;

import com.voctech.model.Theme;
import com.voctech.model.Word;
import com.voctech.model.WordRelation;
import com.voctech.model.WordRelation.RelationType;
import com.voctech.payload.DatabaseRequest;
import com.voctech.payload.ExistingRelationRequest;
import com.voctech.payload.NewRelationRequest;
import com.voctech.repository.ThemeRepository;
import com.voctech.repository.WordRelationRepository;
import com.voctech.repository.WordRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DatabaseService {

  private final WordRepository wordRepository;
  private final WordRelationRepository wordRelationRepository;
  private final ThemeRepository themeRepository;

  /**
   * Ajoute deux nouveaux mots et crée une relation entre eux.
   *
   * @param request Contient les deux mots à ajouter et la relation entre eux.
   * @return ResponseEntity avec un message de succès ou une erreur si les mots existent déjà.
   */
  @Transactional
  public ResponseEntity<?> addWordsWithRelations(DatabaseRequest request) {
    if (
      request.getEntries() == null ||
      request.getEntries().size() != 2 ||
      request.getRelation() == null
    ) {
      return ResponseEntity
        .badRequest()
        .body("{\"error\": \"Format de requête invalide\"}");
    }

    List<DatabaseRequest.WordEntry> wordEntries = request.getEntries();

    for (DatabaseRequest.WordEntry entry : wordEntries) {
      if (
        entry.getWord() == null ||
        entry.getWord().isBlank() ||
        entry.getLanguage() == null ||
        entry.getLanguage().isBlank()
      ) {
        return ResponseEntity
          .badRequest()
          .body("{\"error\": \"Mot ou langue vide\"}");
      }
      ResponseEntity<?> existingWordCheck = checkExistingWord(entry.getWord());
      if (existingWordCheck != null) {
        return existingWordCheck;
      }
    }

    Set<Theme> themes1 = getThemesByIds(wordEntries.get(0).getThemeId());
    Set<Theme> themes2 = getThemesByIds(wordEntries.get(1).getThemeId());

    Word word1 = Word
      .builder()
      .word(wordEntries.get(0).getWord())
      .language(wordEntries.get(0).getLanguage())
      //.themes(getThemesByIds(wordEntries.get(0).getThemeId()))
      .build();
    Word word2 = Word
      .builder()
      .word(wordEntries.get(1).getWord())
      .language(wordEntries.get(1).getLanguage())
      //.themes(getThemesByIds(wordEntries.get(1).getThemeId()))
      .build();

    word1.setThemes(themes1);
    word2.setThemes(themes2);

    wordRepository.save(word1);
    wordRepository.save(word2);

    RelationType relationType = RelationType.valueOf(
      request.getRelation().toUpperCase()
    );

    WordRelation wordRelationAtoB = WordRelation
      .builder()
      .wordSource(word1)
      .wordTarget(word2)
      .type(relationType)
      .build();

    WordRelation wordRelationBtoA = WordRelation
      .builder()
      .wordSource(word2)
      .wordTarget(word1)
      .type(relationType)
      .build();

    wordRelationRepository.save(wordRelationAtoB);
    wordRelationRepository.save(wordRelationBtoA);

    return ResponseEntity
      .ok()
      .body("{\"message\": \"Mots et relation ajoutés avec succès\"}");
  }

  /**
   * Ajoute un nouveau mot et crée une relation avec un mot existant.
   *
   * @param request Contient l'ID du mot source et les informations du nouveau mot.
   * @return ResponseEntity avec un message de succès ou une erreur si le mot cible existe déjà.
   */
  @Transactional
  public ResponseEntity<?> addNewRelation(NewRelationRequest request) {
    if (
      request.getSource() == null ||
      request.getSource().getId() == null ||
      request.getTarget() == null ||
      request.getTarget().getWord() == null ||
      request.getTarget().getWord().isBlank() ||
      request.getTarget().getLanguage() == null ||
      request.getTarget().getLanguage().isBlank() ||
      request.getRelation() == null
    ) {
      return ResponseEntity
        .badRequest()
        .body("{\"error\": \"Format de requête invalide ou champ vide\"}");
    }

    Optional<Word> existingSourceWord = wordRepository.findById(
      request.getSource().getId().longValue()
    );
    if (existingSourceWord.isEmpty()) {
      return ResponseEntity
        .badRequest()
        .body("{\"error\": \"ID du mot source inexistant\"}");
    }

    ResponseEntity<?> existingWordCheck = checkExistingWord(
      request.getTarget().getWord()
    );
    if (existingWordCheck != null) {
      return existingWordCheck;
    }

    Word newWord = Word
      .builder()
      .word(request.getTarget().getWord())
      .language(request.getTarget().getLanguage())
      .themes(getThemesByIds(request.getTarget().getThemeId()))
      .build();
    wordRepository.save(newWord);

    WordRelation wordRelation = WordRelation
      .builder()
      .wordSource(existingSourceWord.get())
      .wordTarget(newWord)
      .type(RelationType.valueOf(request.getRelation().toUpperCase()))
      .build();
    wordRelationRepository.save(wordRelation);

    return ResponseEntity
      .ok()
      .body("{\"message\": \"Nouveau mot et relation ajoutés avec succès\"}");
  }

  /**
   * Ajoute une relation entre deux mots existants.
   *
   * @param request Contient les ID des deux mots et le type de relation.
   * @return ResponseEntity avec un message de succès ou une erreur si l'un des mots n'existe pas.
   */
  @Transactional
  public ResponseEntity<?> addExistingRelation(
    ExistingRelationRequest request
  ) {
    if (
      request.getSource() == null ||
      request.getSource().getId() == null ||
      request.getTarget() == null ||
      request.getTarget().getId() == null ||
      request.getRelation() == null
    ) {
      return ResponseEntity
        .badRequest()
        .body("{\"error\": \"Format de requête invalide ou champ vide\"}");
    }

    Optional<Word> sourceWord = wordRepository.findById(
      request.getSource().getId().longValue()
    );
    if (sourceWord.isEmpty()) {
      return ResponseEntity
        .badRequest()
        .body("{\"error\": \"ID du mot source inexistant\"}");
    }

    Optional<Word> targetWord = wordRepository.findById(
      request.getTarget().getId().longValue()
    );
    if (targetWord.isEmpty()) {
      return ResponseEntity
        .badRequest()
        .body("{\"error\": \"ID du mot cible inexistant\"}");
    }

    WordRelation wordRelation = WordRelation
      .builder()
      .wordSource(sourceWord.get())
      .wordTarget(targetWord.get())
      .type(RelationType.valueOf(request.getRelation().toUpperCase()))
      .build();
    wordRelationRepository.save(wordRelation);

    return ResponseEntity
      .ok()
      .body("{\"message\": \"Relation ajoutée avec succès\"}");
  }

  /**
   * Vérifie si un mot existe déjà dans la base de données.
   *
   * @param word Le mot à vérifier.
   * @return ResponseEntity avec une erreur si le mot existe, null sinon.
   */
  private ResponseEntity<?> checkExistingWord(String word) {
    Optional<Word> existingWord = wordRepository.findByWordIgnoreCase(word);
    if (existingWord.isPresent()) {
      return ResponseEntity
        .badRequest()
        .body("{\"error\": \"Le mot '" + word + "' existe déjà\"}");
    }
    return null;
  }

  /**
   * Récupère un ensemble de thèmes à partir d'une liste d'ID.
   *
   * @param themeIds Liste des ID des thèmes.
   * @return Un ensemble d'objets Theme correspondant aux ID fournis.
   */
  private Set<Theme> getThemesByIds(List<Integer> themeIds) {
    Set<Theme> themes = new HashSet<>();
    if (themeIds != null) {
      themeIds.forEach(id -> themeRepository.findById(id).ifPresent(themes::add)
      );
    }
    return themes;
  }
}
