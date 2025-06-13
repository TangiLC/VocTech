import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class LanguageService {
  private languageSubject = new BehaviorSubject<'fr' | 'en'>('fr');

  // Observable that components can subscribe to
  language$ = this.languageSubject.asObservable();

  // Get current language value
  getCurrentLanguage(): 'fr' | 'en' {
    return this.languageSubject.value;
  }

  // Set language
  setLanguage(lang: 'fr' | 'en') {
    this.languageSubject.next(lang);
  }

  // Toggle language
  toggleLanguage() {
    const currentLang = this.languageSubject.value;
    this.setLanguage(currentLang === 'fr' ? 'en' : 'fr');
  }
}
