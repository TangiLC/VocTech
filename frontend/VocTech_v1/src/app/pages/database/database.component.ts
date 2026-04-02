import {
  Component,
  OnInit,
  OnDestroy,
  ChangeDetectionStrategy,
} from '@angular/core';
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
import {
  Observable,
  of,
  Subject,
  EMPTY,
  switchMap,
  catchError,
  tap,
  startWith,
  takeUntil,
  shareReplay,
} from 'rxjs';
import { ThemeService } from '../../services/theme.service';
import { Theme } from '../../dto/theme.dto';
import { DatabaseService } from '../../services/database.service';
import { WordSearchService } from '../../services/wordSearch.service';
import { WordsTableComponent } from '../../shared/words-table/wordsTable.component';
import { WordResponse } from '../../dto/wordResponse.dto';

@Component({
  selector: 'app-database',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
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
export class AppDatabaseComponent implements OnInit, OnDestroy {
  form!: FormGroup;
  relationForm!: FormGroup;
  themes$!: Observable<Theme[]>;

  private reloadTrigger$ = new Subject<void>();
  private destroy$ = new Subject<void>();

  words$: Observable<WordResponse[]> = this.reloadTrigger$.pipe(
    startWith(null),
    switchMap(() =>
      this.wordSearchService.getLastNWords(6).pipe(
        catchError((err) => {
          console.error('Erreur lors du chargement des mots:', err);
          this.openSnackBar('Erreur lors du chargement des mots', true);
          return of([]);
        })
      )
    ),
    shareReplay(1)
  );

  languages = [
    { value: 'fr', viewValue: 'Français' },
    { value: 'en', viewValue: 'Anglais' },
  ];

  relations = [
    { value: 'translation', viewValue: 'Traduction' },
    { value: 'synonym', viewValue: 'Synonyme' },
    // { value: 'antonym', viewValue: 'Antonyme' },
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
    this.buildForm();
    this.buildRelationForm();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private triggerReload() {
    this.reloadTrigger$.next();
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
      this.databaseService.addPair(entries, relation).pipe(
        tap(() => {
          this.resetForm();
          this.triggerReload();
          this.openSnackBar('Enregistrement effectué avec succès');
        }),
        catchError((err) => {
          console.error(`[${relation}]`, err);
          this.openSnackBar(`Erreur: ${err.error?.error || err.message || 'Unknown'}`, true);
          return EMPTY;
        }),
        takeUntil(this.destroy$)
      ).subscribe();
    } else if (relation === 'synonym') {
      this.databaseService.searchWord(word1).pipe(
        switchMap((results) => {
          if (!results.length) {
            this.openSnackBar(`Mot ${word1} non trouvé`, true);
            return EMPTY;
          }
          const id1 = results[0].id;
          return this.databaseService.addSynonym(id1, {
            word: word2,
            language: language2,
            themeId: themeIds2,
          });
        }),
        tap(() => {
          this.resetForm();
          this.triggerReload();
          this.openSnackBar('Synonyme ajouté avec succès');
        }),
        catchError((err) => {
          console.error('[synonym]', err);
          this.openSnackBar(`Erreur Synonym: ${err.error?.error || err.message || 'Unknown'}`, true);
          return EMPTY;
        }),
        takeUntil(this.destroy$)
      ).subscribe();
    }
  }

  onSubmitRelation() {
    if (this.relationForm.invalid) return;

    const { sourceId, targetId, relationType } = this.relationForm.value;

    this.databaseService
      .addRelation(parseInt(sourceId, 10), parseInt(targetId, 10), relationType)
      .pipe(
        tap(() => {
          this.resetRelationForm();
          this.triggerReload();
          this.openSnackBar('Relation ajoutée avec succès');
        }),
        catchError((err) => {
          console.error('[addRelation]', err);
          this.openSnackBar(`Erreur: ${err.error?.error || err.message || 'Unknown'}`, true);
          return EMPTY;
        }),
        takeUntil(this.destroy$)
      )
      .subscribe();
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
