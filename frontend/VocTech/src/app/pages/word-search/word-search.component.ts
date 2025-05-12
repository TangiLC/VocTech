import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Theme } from '../../dto/theme.dto';
import { ThemeService } from '../../services/theme.service';
import { LanguageService } from '../../services/language.service';
import { ThemeCardComponent } from '../../shared/theme-card/theme-card.component';
import { MatCardModule } from '@angular/material/card';
import { WordSearchInputComponent } from '../../shared/wordSearch-input/wordSearch-input.component';
import { WordsTableComponent } from '../../shared/words-table/wordsTable.component';
import { Observable } from 'rxjs';
import { WordResponse } from '../../dto/wordResponse.dto';

@Component({
  selector: 'app-word-search',
  imports: [
    CommonModule,
    ThemeCardComponent,
    MatCardModule,
    WordSearchInputComponent,
    WordsTableComponent,
  ],
  templateUrl: './word-search.component.html',
  styleUrl: './word-search.component.scss',
})
export class WordSearchComponent implements OnInit {
  private themeService = inject(ThemeService);
  private languageService = inject(LanguageService);

  themes$ = this.themeService.themes$;
  currentLanguage: 'en' | 'fr';
  results$!: Observable<WordResponse[]>;

  constructor() {
    this.currentLanguage = this.languageService.getCurrentLanguage();
  }

  ngOnInit() {
    this.languageService.language$.subscribe((lang) => {
      this.currentLanguage = lang;
      this.themeService.refresh();
    });
  }

  getTitle(): string {
    return this.currentLanguage === 'fr'
      ? "Vocabulaire Technique d'Archéologie"
      : "Technical Vocabulary of Archaeology";
  }

  getContent(): string {
    let contentFr = `La lecture de revues scientifiques, la visite de musées et d'expositions m'ont
    permis d'établir une liste de mots et d'expressions suceptibles de faciliter la traduction.`;
    let contentEn = `Reading scientific journals and visiting museums and exhibitions have enabled
     me to compile a list of words and expressions likely to facilitate translation.`;
    return this.currentLanguage === 'fr' ? contentFr : contentEn;
  }
}
