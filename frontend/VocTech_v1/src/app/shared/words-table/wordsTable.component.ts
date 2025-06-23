import { AuthService } from '../../services/auth.service';
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
import { WordResponse } from '../../dto/wordResponse.dto';
import bgColors from '../theme-card/theme-colors.json';
import { combineLatest, Observable } from 'rxjs';
import { Theme } from '../../dto/theme.dto';
import { decodeTries } from '../../utils/remaining';

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

  isGuest: boolean = true;
  themes: Theme[] = [];
  currentLanguage: 'fr' | 'en';
  displayedColumns: string[] = [];

  constructor(
    private themeService: ThemeService,
    private authService: AuthService,
    private languageService: LanguageService,
    private snackBar: MatSnackBar
  ) {
    this.currentLanguage = this.languageService.getCurrentLanguage();
    this.updateDisplayedColumns();
  }

  dataSource?: WordResponse[];

  ngOnInit() {
    combineLatest([
      this.authService.hasRole$('ROLE_ADMIN'),
      this.authService.hasRole$('ROLE_USER'),
    ]).subscribe(([isAdmin, isUser]) => {
      this.isGuest = !(isAdmin || isUser);
    });

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
    const baseColumns = ['word'];
    const themeColumn = this.isThemeShown ? ['theme'] : [];
    const wordId = this.authService.hasRole$('ROLE_ADMIN')?['id']:[];

    const additionalColumns = ['synonyms', 'translations'];

    this.displayedColumns = [
      ...wordId,
      ...baseColumns,
      ...themeColumn,
      ...additionalColumns,
    ];
  }

  getSynonyms(word: WordResponse): string {
    const synonyms = word.relations?.synonym?.map((s) => s.word);
    return synonyms?.length ? synonyms.join(', ') : '-';
  }

  /*getTranslations(word: WordResponse): string {
    const translations = word.relations?.translation?.map((t) => t.word);
    return translations?.length ? translations.join(', ') : '-';
  }*/
  getTranslationList(word: WordResponse): string[] {
    return (
      word.relations?.translation?.map((t) => t.language + ' ' + t.word) ?? []
    );
  }

  getSplitIndex(word: string): number {
    return Math.min(4, word.length);
  }

  getTriesLeft(): number {
    if (this.isGuest) {
      const encodedRemainQ = localStorage.getItem('remainQ');
      return encodedRemainQ ? decodeTries(encodedRemainQ) : 0;
    } else {
      return 10;
    }
  }

  getLanguageFlag(lang: string): string {
    switch (lang.toLowerCase()) {
      case 'fr':
        return '🇫🇷';
      case 'en':
        return '🇬🇧';
      case 'it':
        return '🇮🇹';
      case 'de':
        return '🇩🇪';
      case 'jp':
        return '🇯🇵';
      case 'es':
        return '🇪🇸';
      default:
        return lang;
    }
  }

  splitData(data: string): [string, string, string] {
    const triesLeft = this.getTriesLeft();

    const languagePart = data.slice(0, 2);
    const word = data.slice(2);

    if (triesLeft > 3) {
      return [languagePart, word, ''];
    }

    let lettersToKeep: number = 1;

    if (triesLeft <= 0) {
      lettersToKeep = 1;
    } else {
      let keepPercentage: number;
      switch (triesLeft) {
        case 3:
          keepPercentage = 0.75;
          break;
        case 2:
          keepPercentage = 0.45;
          break;
        case 1:
          keepPercentage = 0.25;
          break;
        default:
          keepPercentage = 1;
          break;
      }

      const calculatedKeep = Math.ceil(word.length * keepPercentage);
      lettersToKeep = word.length < 3 ? 1 : Math.max(1, calculatedKeep);
    }

    const visiblePart = word.slice(0, lettersToKeep+1);
    const maskedPart = '*'.repeat(word.length - lettersToKeep-1);

    return [languagePart, visiblePart, maskedPart];
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
    let toClipboard = word.slice(2);
    let mssg =
      this.currentLanguage == 'fr'
        ? ['Copie du mot', 'dans le presse-papier', 'Erreur de copie']
        : ['Copy of word', 'into clipboard', 'Copy error'];
    navigator.clipboard
      .writeText(toClipboard)
      .then(() => {
        this.snackBar.open(`${mssg[0]} « ${toClipboard} » ${mssg[1]}`, '', {
          duration: 2000,
        });
      })
      .catch((err) => {
        this.snackBar.open(`${mssg[2]}`, '', { duration: 2000 });
      });
  }
}
