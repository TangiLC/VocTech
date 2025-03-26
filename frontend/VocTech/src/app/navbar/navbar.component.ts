import { CommonModule, isPlatformBrowser } from '@angular/common';
import {
  Component,
  computed,
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
  standalone: true,
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
      word: 'Mot',
      database: 'Base de données',
      login: 'Connexion',
      logout: 'Déconnexion',
      menu: 'Menu',
    },
    en: {
      theme: 'Theme',
      word: 'Word',
      database: 'Database',
      login: 'Login',
      logout: 'Logout',
      menu: 'Menu',
    },
  };

  currentUrl = signal<string>('');
  isMenuOpen = signal<boolean>(false);
  isMobileView = signal<boolean>(false);

  isHomePage = computed(
    () =>
      this.currentUrl() === '/home' ||
      this.currentUrl() === '/' ||
      this.currentUrl() === '/login'
  );

  constructor(private authService: AuthService) {
    if (this.isBrowser) {
      this.checkScreenWidth();
      window.addEventListener('resize', this.checkScreenWidth.bind(this));
    }
  }

  checkScreenWidth(): void {
    this.isMobileView.set(window.innerWidth < 768);
  }

  ngOnInit() {
    this.router.events
      .pipe(filter((event) => event instanceof NavigationEnd))
      .subscribe(() => {
        this.currentUrl.set(this.router.url);
        // Close menu on navigation
        this.isMenuOpen.set(false);
      });
  }

  toggleLanguage(): void {
    this.languageService.toggleLanguage();
  }

  toggleMenu(): void {
    this.isMenuOpen.set(!this.isMenuOpen());
  }

  goToLogin(): void {
    this.router.navigate(['/login']);
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/home']);
    console.log('Déconnecté');
  }

  navigateTo(route: string): void {
    this.router.navigate([route]);
    this.isMenuOpen.set(false);
  }

  ngOnDestroy(): void {
    if (this.isBrowser) {
      window.removeEventListener('resize', this.checkScreenWidth.bind(this));
    }
  }
}
