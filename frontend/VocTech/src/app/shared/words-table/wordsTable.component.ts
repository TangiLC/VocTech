import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { ThemeService } from '../../services/theme.service';
import { LanguageService } from '../../services/language.service';
import { WordResponse } from './../../dto/wordResponse.dto';
import bgColors from '../theme-card/theme-colors.json';
import { Observable } from 'rxjs';
import { Theme } from '../../dto/theme.dto';

@Component({
  selector: 'app-word-table',
  standalone: true,
  imports: [CommonModule, MatTableModule],
  templateUrl: './wordsTable.component.html',
  styleUrls: ['./wordsTable.component.scss'],
})
export class WordsTableComponent implements OnInit {
  @Input() data$!: Observable<WordResponse[]>;
  themes: Theme[] = [];
  currentLanguage: 'fr' | 'en';

  constructor(
    private themeService: ThemeService,
    private languageService: LanguageService
  ) {
    this.currentLanguage = this.languageService.getCurrentLanguage();
  }

  displayedColumns: string[] = [
    'word',
    'language',
    'theme',
    'synonyms',
    'translations',
    'translationLanguages',
  ];
  dataSource: WordResponse[] = [];

  ngOnInit() {
    this.themeService.themes$.subscribe((themes) => (this.themes = themes));
    this.languageService.language$.subscribe(
      (lang) => (this.currentLanguage = lang)
    );
    this.data$.subscribe((data) => {
      console.log('Received data:', data);
      this.dataSource = data;
    });
  }

  getSynonyms(word: WordResponse): string {
    //console.log('Word for synonyms:', word);
    //console.log('Synonyms:', word.relations?.synonym);

    const synonyms = word.relations?.synonym?.map((s) => s.word);
    //console.log('Mapped synonyms:', synonyms);

    return synonyms?.length ? synonyms.join(', ') : '-';
  }

  getTranslations(word: WordResponse): string {
    //console.log('Word for translations:', word);
    //console.log('Translations:', word.relations?.translation);

    const translations = word.relations?.translation?.map((t) => t.word);
    //console.log('Mapped translations:', translations);

    return translations?.length ? translations.join(', ') : '-';
  }

  getTranslationLanguages(word: WordResponse): string {
    //console.log('Word for translation languages:', word);
    //console.log('Translations:', word.relations?.translation);

    const langs = new Set(
      word.relations?.translation?.map((t) => t.language) || []
    );
    //console.log('Translation languages:', langs);

    return langs.size ? Array.from(langs).join(', ') : '-';
  }

  getThemeName(themeId: number): string {
    if (!this.themes || this.themes.length === 0) {
      return '';
    }
    const theme = this.themes.find((t) => t.id == themeId);
    //console.log('THEMES', this.themes);
    if (!theme) return themeId.toString();
    let themeName=this.currentLanguage === 'fr' ? theme.nameFr : theme.nameEn;
    return themeName.slice(0,4)}


  getThemeColor(themeId: number): string {
    const rgb = bgColors.theme[themeId];
    return rgb ? `rgba(${rgb[0]}, ${rgb[1]}, ${rgb[2]}, 0.3)` : 'transparent';
  }
}
