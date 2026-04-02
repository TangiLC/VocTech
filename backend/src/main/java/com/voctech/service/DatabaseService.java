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

    List<DatabaseRequest.WordEntry> wordEntriesList = request.getEntries();
    Word word1 = createAndSaveWord(wordEntriesList.get(0));
    Word word2 = createAndSaveWord(wordEntriesList.get(1));

    RelationType relationType = RelationType.valueOf(
      request.getRelation().toLowerCase()
    );

    createSymmetricRelation(word1, word2, relationType);

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

    RelationType newRelationType = RelationType.valueOf(
      request.getRelation().toLowerCase()
    );
    Word existingSource = existingSourceWord.get();

    // Création de la relation de base entre A et E (de manière symétrique)
    createSymmetricRelation(existingSource, newWord, newRelationType);

    // Propagation des relations existantes de A vers E
    Set<WordRelation> aRelations = existingSource.getSourceRelations();
    if (aRelations != null) {
      if (newRelationType == RelationType.synonym) {
        // Pour A synonym E, on propage :
        // - A synonym B  -> E synonym B
        // - A antonym C  -> E antonym C
        // - A translation D -> E translation D
        for (WordRelation rel : aRelations) {
          RelationType relType = rel.getType();
          Word relatedWord = rel.getWordTarget();
          if (relType == RelationType.synonym) {
            createSymmetricRelation(newWord, relatedWord, RelationType.synonym);
          } else if (relType == RelationType.antonym) {
            createSymmetricRelation(newWord, relatedWord, RelationType.antonym);
          } else if (relType == RelationType.translation) {
            createSymmetricRelation(
              newWord,
              relatedWord,
              RelationType.translation
            );
          }
        }
      } else if (newRelationType == RelationType.antonym) {
        // Pour A antonym E, on propage :
        // - A synonym B  -> E antonym B
        // - A antonym C  -> E synonym C
        for (WordRelation rel : aRelations) {
          RelationType relType = rel.getType();
          Word relatedWord = rel.getWordTarget();
          if (relType == RelationType.synonym) {
            createSymmetricRelation(newWord, relatedWord, RelationType.antonym);
          } else if (relType == RelationType.antonym) {
            createSymmetricRelation(newWord, relatedWord, RelationType.synonym);
          }
        }
      } else if (newRelationType == RelationType.translation) {
        // Pour A translation E, on propage pour chaque traduction D de A :
        // Si D.language == E.language, alors D et E sont synonymes,
        // sinon, D et E sont en translation.
        for (WordRelation rel : aRelations) {
          if (rel.getType() == RelationType.translation) {
            Word relatedWord = rel.getWordTarget();
            if (
              relatedWord.getLanguage().equalsIgnoreCase(newWord.getLanguage())
            ) {
              createSymmetricRelation(
                newWord,
                relatedWord,
                RelationType.synonym
              );
            } else {
              createSymmetricRelation(
                newWord,
                relatedWord,
                RelationType.translation
              );
            }
          }
        }
      }
    }

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
      .type(RelationType.valueOf(request.getRelation().toLowerCase()))
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

  /**
   * Crée une relation entre deux mots et sa relation symétrique.
   *
   * @param wordA Le premier mot
   * @param wordB Le second mot
   * @param type Le type de relation
   */
  private void createSymmetricRelation(
    Word wordA,
    Word wordB,
    RelationType type
  ) {
    if (wordA.getId().equals(wordB.getId())) {
        return;
    }
    if (
      !wordRelationRepository.existsByWordSourceAndWordTargetAndType(
        wordA,
        wordB,
        type
      )
    ) {
      WordRelation relAB = WordRelation
        .builder()
        .wordSource(wordA)
        .wordTarget(wordB)
        .type(type)
        .build();
      wordRelationRepository.save(relAB);
    }
    // Relation B→A
    if (
      !wordRelationRepository.existsByWordSourceAndWordTargetAndType(
        wordB,
        wordA,
        type
      )
    ) {
      WordRelation relBA = WordRelation
        .builder()
        .wordSource(wordB)
        .wordTarget(wordA)
        .type(type)
        .build();
      wordRelationRepository.save(relBA);
    }
  }

  /**
   * Crée et sauvegarde un mot à partir d'une entrée, en lui associant ses thèmes.
   *
   * @param entry l'entrée contenant le mot, la langue et la liste d'ID des thèmes.
   * @return le mot sauvegardé.
   */
  private Word createAndSaveWord(DatabaseRequest.WordEntry entry) {
    Set<Theme> themes = getThemesByIds(entry.getThemeId());
    Word word = Word
      .builder()
      .word(entry.getWord())
      .language(entry.getLanguage())
      .build();
    word.setThemes(themes);
    return wordRepository.save(word);
  }
}
