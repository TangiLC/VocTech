// admin.guard.ts
import { Injectable, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Injectable({
  providedIn: 'root',
})
export class AdminGuard {
  private router = inject(Router);
  private authService = inject(AuthService);

  canActivate(): boolean {
    // Vérifie si l'utilisateur est authentifié et a le rôle admin
    if (
      this.authService.isAuthenticated$() &&
      this.authService.hasRole$('ROLE_ADMIN')
    ) {
      return true;
    }

    if (this.authService.isAuthenticated$()) {
      this.router.navigate(['/access-denied']);
      return false;
    }

    this.router.navigate(['/login']);
    return false;
  }
}
