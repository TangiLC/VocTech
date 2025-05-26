import {
  Component,
  Input,
  OnChanges,
  OnInit,
  SimpleChanges,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatSnackBarModule, MatSnackBar } from '@angular/material/snack-bar';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { ThemeService } from '../../services/theme.service';
import { LanguageService } from '../../services/language.service';
import { WordResponse } from './../../dto/wordResponse.dto';
import bgColors from '../theme-card/theme-colors.json';
import { Observable } from 'rxjs';
import { Theme } from '../../dto/theme.dto';

@Component({
  selector: 'app-word-table',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatSnackBarModule,
    MatIconModule,
    MatButtonModule,
  ],
  templateUrl: './wordsTable.component.html',
  styleUrls: ['./wordsTable.component.scss'],
})
export class WordsTableComponent implements OnInit, OnChanges {
  @Input() data$!: Observable<WordResponse[]>;
  @Input() isThemeShown = false;

  themes: Theme[] = [];
  currentLanguage: 'fr' | 'en';
  displayedColumns: string[] = [];

  constructor(
    private themeService: ThemeService,
    private languageService: LanguageService,
    private snackBar: MatSnackBar
  ) {
    this.currentLanguage = this.languageService.getCurrentLanguage();
    this.updateDisplayedColumns();
  }

  dataSource?: WordResponse[] ;

  ngOnInit() {
    this.themeService.themes$.subscribe((themes) => (this.themes = themes));
    this.languageService.language$.subscribe(
      (lang) => (this.currentLanguage = lang)
    );
    this.data$.subscribe((data) => {
      console.log('Received data:', data);
      this.dataSource = data;
    });
    this.updateDisplayedColumns();
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['isThemeShown']) {
      this.updateDisplayedColumns();
    }
  }

  updateDisplayedColumns() {
    const baseColumns = ['word', 'language'];
    const themeColumn = this.isThemeShown ? ['theme'] : [];

    const additionalColumns = [
      'synonyms',
      'translations',
      'translationLanguages',
    ];

    this.displayedColumns = [
      ...baseColumns,
      ...themeColumn,
      ...additionalColumns,
    ];
  }

  getSynonyms(word: WordResponse): string {
    const synonyms = word.relations?.synonym?.map((s) => s.word);
    return synonyms?.length ? synonyms.join(', ') : '-';
  }

  getTranslations(word: WordResponse): string {
    const translations = word.relations?.translation?.map((t) => t.word);
    return translations?.length ? translations.join(', ') : '-';
  }
  getTranslationList(word: WordResponse): string[] {
    return word.relations?.translation?.map((t) => t.word) ?? [];
  }

  getTranslationLanguages(word: WordResponse): string {
    const langs = new Set(
      word.relations?.translation?.map((t) => t.language) || []
    );
    return langs.size ? Array.from(langs).join(', ') : '-';
  }

  getThemeName(themeId: number): string {
    if (!this.themes || this.themes.length === 0) {
      return '';
    }
    const theme = this.themes.find((t) => t.id == themeId);
    if (!theme) return themeId.toString();
    let themeName = this.currentLanguage === 'fr' ? theme.nameFr : theme.nameEn;
    return themeName;
  }

  getThemeColor(themeId: number): string {
    const rgb = bgColors.theme[themeId];
    return rgb ? `rgba(${rgb[0]}, ${rgb[1]}, ${rgb[2]}, 0.3)` : 'transparent';
  }

  getNoDataText(): string {
    return this.currentLanguage === 'fr'
      ? 'Pas de correspondance'
      : 'No matching data available';
  }

  copyWord(word: string): void {
    let mssg =
      this.currentLanguage == 'fr'
        ? ['Copie du mot', 'dans le presse-papier', 'Erreur de copie']
        : ['Copy of word', 'into clipboard', 'Copy error'];
    navigator.clipboard
      .writeText(word)
      .then(() => {
        this.snackBar.open(`${mssg[0]} « ${word} » ${mssg[1]}`, '', {
          duration: 2000,
        });
      })
      .catch((err) => {
        this.snackBar.open(`${mssg[2]}`, '', { duration: 2000 });
      });
  }
}
