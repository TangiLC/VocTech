// theme.component.ts
import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Theme } from '../../dto/theme.dto';
import { ThemeService } from '../../services/theme.service';
import { LanguageService } from '../../services/language.service';
import { ThemeCardComponent } from '../../shared/theme-card/theme-card.component';
import { RemainingQueryComponent } from '../../shared/remaining-query/remaining-query.component';

@Component({
  selector: 'app-theme',
  standalone: true,
  imports: [ CommonModule, ThemeCardComponent,RemainingQueryComponent ],
  templateUrl: './theme.component.html',
  styleUrls: ['./theme.component.scss'],
})
export class ThemeComponent implements OnInit {
  private themeService = inject(ThemeService);
  private languageService = inject(LanguageService);

  themes$ = this.themeService.themes$;
  currentLanguage: 'en' | 'fr';

  constructor() {
    this.currentLanguage = this.languageService.getCurrentLanguage();
  }

  ngOnInit() {
    this.languageService.language$.subscribe(lang => {
      this.currentLanguage = lang;
      this.themeService.refresh();
    });
  }
}
