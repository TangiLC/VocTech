import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { map } from 'rxjs/operators';
import { Theme } from '../../dto/theme.dto';
import { ThemeService } from '../../services/theme.service';
import { LanguageService } from '../../services/language.service';
//import { ThemeCardComponent } from '../theme-card/theme-card.component';

interface AppState {
  themes: Theme[];
}

@Component({
  selector: 'app-theme',
  standalone: true,
  imports: [
    CommonModule,
    //ThemeCardComponent
  ],
  templateUrl: './theme.component.html',
  styleUrls: ['./theme.component.scss'],
})
export class ThemeComponent implements OnInit {
  private themeService = inject(ThemeService);
  private languageService = inject(LanguageService);

  themes$ = new BehaviorSubject<Theme[]>([]);
  currentLanguage: 'en' | 'fr';

  constructor() {
    this.currentLanguage = this.languageService.getCurrentLanguage();
  }

  ngOnInit() {
    this.themeService.fetchThemes().subscribe((themes) => {
      this.themes$.next(themes);
    });
    this.languageService.language$.subscribe(
      (lang) => (this.currentLanguage = lang)
    );
  }
}
