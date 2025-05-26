import {
  Component,
  EventEmitter,
  Input,
  OnDestroy,
  OnInit,
  Output,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  debounceTime,
  distinctUntilChanged,
  filter,
  switchMap,
  map,
} from 'rxjs/operators';
import { Observable, Subscription, EMPTY } from 'rxjs';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatListModule } from '@angular/material/list';
import { WordSearchService } from '../../services/wordSearch.service';
import { WordResponse } from '../../dto/wordResponse.dto';
import { LanguageService } from '../../services/language.service';

@Component({
  selector: 'app-word-search-input',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatListModule,
  ],
  templateUrl: './wordSearch-input.component.html',
  styleUrls:['./wordSearch-input.component.scss']
})
export class WordSearchInputComponent implements OnInit, OnDestroy {
  @Input() themeId?: number;
  @Output() results = new EventEmitter<Observable<WordResponse[]>>();

  searchControl = new FormControl('', [Validators.pattern(/^[\w\s\-’?]+$/)]);
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
    private languageService: LanguageService
  ) {}

  ngOnInit() {
    this.langSub = this.languageService.language$.subscribe((lang) => {
      this.currentLanguage = lang;
    });

    const results$ = this.searchControl.valueChanges.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      filter((value) => this.isValidInput(value)),
      switchMap((value) => this.wordSearchService.searchWords(value || '')),
      map((words) =>
        this.themeId
          ? words.filter((w) => w.themeId.includes(this.themeId!))
          : words
      )
    );

    this.results.emit(results$);
  }

  ngOnDestroy() {
    this.langSub?.unsubscribe();
  }

  isValidInput(value: string | null): boolean {
    return !!value && value.length >= 3 && this.searchControl.valid;
  }

  get currentLabels() {
    return this.labels[this.currentLanguage];
  }
}
