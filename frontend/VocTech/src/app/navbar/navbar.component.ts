import { CommonModule, isPlatformBrowser } from '@angular/common';
import {
  Component,
  computed,
  effect,
  inject,
  PLATFORM_ID,
  signal,
  OnInit,
} from '@angular/core';
import { AuthService } from '../services/auth.service';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { Router, RouterModule, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';
import { LanguageService } from '../services/language.service';

@Component({
  selector: 'app-navbar',
  standalone: true, // Assurez-vous d'utiliser le mode standalone
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
export class NavbarComponent implements OnInit {
  private router = inject(Router);
  private isBrowser = isPlatformBrowser(inject(PLATFORM_ID));
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

  constructor(private authService: AuthService) {}

  ngOnInit() {
    // Écouter les événements de navigation pour mettre à jour l'URL
    this.router.events
      .pipe(filter((event) => event instanceof NavigationEnd))
      .subscribe(() => {
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
    this.router.navigate(['/home']);
    console.log('Déconnecté');
  }
}
