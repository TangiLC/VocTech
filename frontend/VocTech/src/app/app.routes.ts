import { Routes } from '@angular/router';
import { provideRouter } from '@angular/router';
import { bootstrapApplication } from '@angular/platform-browser';
import { AppComponent } from './app.component';
import { AuthGuard } from './guards/auth.guard';
import { AdminGuard } from './guards/admin.guard';
import { inject } from '@angular/core';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () =>
      import('./pages/login/login.component').then((c) => c.LoginComponent),
  },
  {
    path: 'themes',
    loadComponent: () =>
      import('./pages/theme/theme.component').then((c) => c.ThemeComponent),
    canActivate: [() => inject(AuthGuard).canActivate()],
  },
  {
    path: 'search',
    loadComponent: () =>
      import('./pages/word-search/word-search.component').then(
        (c) => c.WordSearchComponent
      ),
    canActivate: [() => inject(AuthGuard).canActivate()],
  },
  {
    path: 'database',
    loadComponent: () =>
      import('./pages/database/database.component').then(
        (c) => c.DatabaseComponent
      ),
    canActivate: [
      () => inject(AuthGuard).canActivate(),
      () => inject(AdminGuard).canActivate(),
    ],
  },
  {
    path: '**',
    redirectTo: 'login',
  },
];

// Bootstrap en mode standalone
bootstrapApplication(AppComponent, {
  providers: [provideRouter(routes)],
});
