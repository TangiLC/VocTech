import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  ReactiveFormsModule,
  FormBuilder,
  FormGroup,
  Validators,
} from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatCardModule } from '@angular/material/card';
import { MatRadioModule } from '@angular/material/radio';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBarModule, MatSnackBar } from '@angular/material/snack-bar';
import { Observable } from 'rxjs';
import { ThemeService } from '../../services/theme.service';
import { Theme } from '../../dto/theme.dto';
import { DatabaseService } from '../../services/database.service';
import { WordSearchService } from '../../services/wordSearch.service';
import { WordsTableComponent } from '../../shared/words-table/wordsTable.component';
import { WordResponse } from '../../dto/wordResponse.dto';

@Component({
  selector: 'app-database',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatCardModule,
    MatInputModule,
    MatSelectModule,
    MatRadioModule,
    MatButtonModule,
    MatSnackBarModule,
    WordsTableComponent,
  ],
  templateUrl: './database.component.html',
  styleUrls: ['./database.component.scss'],
})
export class AppDatabaseComponent {
  form!: FormGroup;
  relationForm!: FormGroup;
  themes$!: Observable<Theme[]>;
  words$!: Observable<WordResponse[]>;
  languages = [
    { value: 'fr', viewValue: 'Français' },
    { value: 'en', viewValue: 'Anglais' },
  ];
  relations = [
    { value: 'translation', viewValue: 'Traduction' },
    { value: 'synonym', viewValue: 'Synonyme' },
    //{ value: 'antonym', viewValue: 'Antonyme' },
  ];

  constructor(
    private fb: FormBuilder,
    private databaseService: DatabaseService,
    private wordSearchService: WordSearchService,
    private themeService: ThemeService,
    private snackBar: MatSnackBar
  ) {
    this.themes$ = this.themeService.themes$;
  }

  ngOnInit(): void {
    this.loadWords();
    this.buildForm();
    this.buildRelationForm();
  }

  private loadWords(): void {
    this.words$ = this.wordSearchService.getLastNWords(6);
  }

  private buildForm() {
    this.form = this.fb.group({
      word1: ['', Validators.required],
      language1: ['fr', Validators.required],
      themeIds1: [[], Validators.required],
      word2: ['', Validators.required],
      language2: ['en', Validators.required],
      themeIds2: [[], Validators.required],
      relation: ['translation', Validators.required],
    });
  }

  private buildRelationForm() {
    this.relationForm = this.fb.group({
      sourceId: ['', [Validators.required, Validators.pattern(/^\d+$/)]],
      targetId: ['', [Validators.required, Validators.pattern(/^\d+$/)]],
      relationType: ['translation', Validators.required],
    });
  }

  onSubmit() {
    if (this.form.invalid) return;

    const {
      word1,
      language1,
      themeIds1,
      word2,
      language2,
      themeIds2,
      relation,
    } = this.form.value;
    const entries = [
      { word: word1, language: language1, themeId: themeIds1 },
      { word: word2, language: language2, themeId: themeIds2 },
    ];

    if (relation === 'translation' || relation === 'antonym') {
      this.databaseService.addPair(entries, relation).subscribe({
        next: () => {
          this.resetForm();
          this.loadWords(); // Recharger les mots après succès
          this.openSnackBar('Enregistrement effectué avec succès');
        },
        error: (err) => {
          console.error(`[${relation}]`, err);
          const errorMessage = err.error?.error || err.message || 'Unknown';
          this.openSnackBar(`Erreur: ${errorMessage}`, true);
        },
      });
    } else if (relation === 'synonym') {
      this.databaseService.searchWord(word1).subscribe({
        next: (results) => {
          if (!results.length) {
            this.openSnackBar(`Mot ${word1} non trouvé`, true);
            return;
          }
          const id1 = results[0].id;
          this.databaseService
            .addSynonym(id1, {
              word: word2,
              language: language2,
              themeId: themeIds2,
            })
            .subscribe({
              next: () => {
                this.resetForm();
                this.loadWords(); // Recharger les mots après succès
                this.openSnackBar('Synonyme ajouté avec succès');
              },
              error: (err) => {
                console.error('[synonym]', err);
                const errorMessage =
                  err.error?.error || err.message || 'Unknown';
                this.openSnackBar(`Erreur Synonym: ${errorMessage}`, true);
              },
            });
        },
        error: (err) => {
          console.error('[search word1]', err);
          const errorMessage = err.error?.error || err.message || 'Unknown';
          this.openSnackBar(`Erreur Word1: ${errorMessage}`, true);
        },
      });
    }
  }

  onSubmitRelation() {
    if (this.relationForm.invalid) return;

    const { sourceId, targetId, relationType } = this.relationForm.value;

    this.databaseService
      .addRelation(parseInt(sourceId, 10), parseInt(targetId, 10), relationType)
      .subscribe({
        next: () => {
          this.resetRelationForm();
          this.loadWords(); // Recharger les mots après succès
          this.openSnackBar('Relation ajoutée avec succès');
        },
        error: (err) => {
          console.error('[addRelation]', err);
          const errorMessage = err.error?.error || err.message || 'Unknown';
          this.openSnackBar(`Erreur: ${errorMessage}`, true);
        },
      });
  }

  private openSnackBar(message: string, isError: boolean = false) {
    this.snackBar.open(message, 'OK', {
      duration: isError ? 5000 : 3000,
      panelClass: isError ? ['error-snackbar'] : ['success-snackbar'],
    });
  }

  private resetForm() {
    this.form.reset({
      word1: '',
      language1: 'fr',
      themeIds1: [],
      word2: '',
      language2: 'en',
      themeIds2: [],
      relation: 'translation',
    });
  }

  private resetRelationForm() {
    this.relationForm.reset({
      sourceId: '',
      targetId: '',
      relationType: 'translation',
    });
  }
}
