import { CommonModule, isPlatformBrowser } from '@angular/common';
import {
  Component,
  computed,
  effect,
  inject,
  PLATFORM_ID,
  signal,
} from '@angular/core';
import { AuthService } from '../services/auth.service';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { Router, RouterModule } from '@angular/router';
import { BehaviorSubject, filter } from 'rxjs';
import { LanguageService } from '../services/language.service';

@Component({
  selector: 'app-navbar',
  imports: [
    CommonModule,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    RouterModule,
  ],
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss'],
})
export class NavbarComponent {
  private router = inject(Router);
  private isBrowser = isPlatformBrowser(inject(PLATFORM_ID));
  private languageSubject = new BehaviorSubject<'fr' | 'en'>('fr');

  languageService = inject(LanguageService);
  labels = {
    fr: {
      theme: 'Thème',
      mot: 'Mot',
      database: 'Base de données',
      login: 'Connexion',
      logout: 'Déconnexion',
    },
    en: {
      theme: 'Theme',
      mot: 'Word',
      database: 'Database',
      login: 'login',
      logout: 'logout',
    },
  };
  currentUrl = signal<string>('');

  isHomePage = computed(
    () => this.currentUrl() === '/home' || this.currentUrl() === '/'
  );
  isNavbarHidden = computed(
    () =>
      this.currentUrl().startsWith('/auth/') &&
      (this.isBrowser ? window.innerWidth < 758 : false)
  );

  constructor(private authService: AuthService) {
    effect(() => {
      this.currentUrl.set(this.router.url);
    });
  }

  toggleLanguage(): void {
    this.languageService.toggleLanguage();
  }
  goToLogin(): void {
    this.router.navigate(['/login']);
  }

  logout(): void {
    this.authService.logout();
    console.log('Déconnecté');
  }
}
