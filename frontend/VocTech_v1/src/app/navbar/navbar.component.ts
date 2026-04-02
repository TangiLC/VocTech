import { CommonModule, isPlatformBrowser } from '@angular/common';
import {
  Component,
  computed,
  inject,
  PLATFORM_ID,
  signal,
  OnInit,
  OnDestroy,
} from '@angular/core';
import { AuthService } from '../services/auth.service';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { Router, RouterModule, NavigationEnd } from '@angular/router';
import { filter, takeUntil } from 'rxjs/operators';
import { LanguageService } from '../services/language.service';
import { BehaviorSubject, Subject } from 'rxjs';

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
export class NavbarComponent implements OnInit, OnDestroy {
  private router = inject(Router);
  private isBrowser = isPlatformBrowser(inject(PLATFORM_ID));
  languageService = inject(LanguageService);
  private destroy$ = new Subject<void>();

  labels = {
    fr: {
      theme: 'Les Thèmes',
      word: 'Recherche',
      database: 'Base de données',
      login: 'Connexion',
      logout: 'Déconnexion',
      menu: 'Menu',
    },
    en: {
      theme: 'Themes',
      word: 'Search',
      database: 'Database',
      login: 'Login',
      logout: 'Logout',
      menu: 'Menu',
    },
  };

  currentUrl = signal<string>('');
  isMenuOpen = signal<boolean>(false);
  isMobileView = signal<boolean>(false);

  private isAdminSubject = new BehaviorSubject<boolean>(false);
  isAdmin$ = this.isAdminSubject.asObservable();

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
    // Écouter les changements de navigation
    this.router.events
      .pipe(
        filter((event) => event instanceof NavigationEnd),
        takeUntil(this.destroy$)
      )
      .subscribe(() => {
        this.currentUrl.set(this.router.url);
        // Close menu on navigation
        this.isMenuOpen.set(false);
      });

    this.authService
      .hasRole$('ADMIN')
      .pipe(takeUntil(this.destroy$))
      .subscribe((isAdmin) => {
        this.isAdminSubject.next(isAdmin);
      });
  }

  private updateAdminStatus(): void {
    this.authService
      .hasRole$('ADMIN')
      .pipe(takeUntil(this.destroy$))
      .subscribe((isAdmin) => {
        const currentValue = this.isAdminSubject.value;
        if (isAdmin !== currentValue) {
          this.isAdminSubject.next(isAdmin);
        }
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
    console.log('Déconnecté');
  }

  navigateTo(route: string): void {
    this.router.navigate([route]);
    this.isMenuOpen.set(false);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();

    if (this.isBrowser) {
      window.removeEventListener('resize', this.checkScreenWidth.bind(this));
      window.removeEventListener('storage', this.updateAdminStatus.bind(this));
    }
  }
}
