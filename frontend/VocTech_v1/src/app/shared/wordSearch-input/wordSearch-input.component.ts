import {
  Component,
  EventEmitter,
  Input,
  OnDestroy,
  OnInit,
  Output,
  HostListener,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  debounceTime,
  distinctUntilChanged,
  filter,
  switchMap,
  map,
  take,
} from 'rxjs/operators';
import { Observable, Subscription, EMPTY, fromEvent } from 'rxjs';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIcon } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { WordSearchService } from '../../services/wordSearch.service';
import { WordResponse } from '../../dto/wordResponse.dto';
import { LanguageService } from '../../services/language.service';
import { decodeTries, encodeTries } from '../../utils/remaining';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-word-search-input',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatListModule,
    MatIcon,
  ],
  templateUrl: './wordSearch-input.component.html',
  styleUrls: ['./wordSearch-input.component.scss'],
})
export class WordSearchInputComponent implements OnInit, OnDestroy {
  @Input() themeId?: number;
  @Output() results = new EventEmitter<Observable<WordResponse[]>>();

  searchControl = new FormControl('', [
    Validators.pattern(/^[\w\s\-’?’àâäéèêëïîôöùûüçœæÀÂÄÉÈÊËÏÎÔÖÙÛÜÇŒÆ]+$/),
  ]);
  results$!: Observable<WordResponse[]>;

  currentLanguage: 'en' | 'fr' = 'fr'; // valeur par défaut
  private langSub!: Subscription;

  labels = {
    en: {
      label: 'Search a word (3 letters min)',
      placeholder: 'Type a word...',
      patternError:
        "Only alphanumeric characters, dashes (-), apostrophes (') and question marks (?) are allowed.",
      noResults: 'No result found.',
    },
    fr: {
      label: 'Rechercher un mot (au moins 3 lettres)',
      placeholder: 'Tapez un mot...',
      patternError:
        "Seuls les caractères alphanumériques, tirets (-), apostrophes (') et points d’interrogation (?) sont autorisés.",
      noResults: 'Aucun résultat trouvé.',
    },
  };

  constructor(
    private wordSearchService: WordSearchService,
    private languageService: LanguageService,
    private authService: AuthService
  ) {}

  ngOnInit() {
    this.langSub = this.languageService.language$.subscribe((lang) => {
      this.currentLanguage = lang;
    });

    this.authService.remainingQueries$.subscribe((remain) => {
      if (remain <= 0) {
        this.searchControl.disable({ emitEvent: false });
      } else {
        this.searchControl.enable({ emitEvent: false });
      }
    });

    const results$ = this.searchControl.valueChanges.pipe(
      filter((value): value is string => !!value && value.trim().length > 0),
      map((value) => value.normalize('NFD').replace(/[\u0300-\u036f]/g, '')),
      debounceTime(300),
      distinctUntilChanged(),
      filter((value) => this.isValidInput(value)),
      switchMap((value) =>
        this.authService.remainingQueries().pipe(
          take(1),
          filter((remaining) => remaining > 0),
          switchMap(() => this.wordSearchService.searchWords(value || '')),
          map((words) =>
            this.themeId
              ? words.filter((w) => w.themeId.includes(this.themeId!))
              : words
          )
        )
      )
    );

    this.results.emit(results$);
  }

  onEnterPress(): void {
    //if (event.key !== 'Enter') return;

    this.authService
      .remainingQueries()
      .pipe(take(1))
      .subscribe((current) => {
        const updated = current - 1;
        this.authService.updateRemainingQueries(updated);
      });
  }

  ngOnDestroy() {
    this.langSub?.unsubscribe();
  }

  isValidInput(value: string | null | undefined): boolean {
    return !!value && value.length >= 3 && this.searchControl.valid;
  }

  get currentLabels() {
    return this.labels[this.currentLanguage];
  }
}
