// admin.guard.ts
import { Injectable, inject } from '@angular/core';
import { Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { AuthService } from '../services/auth.service';

@Injectable({
  providedIn: 'root',
})
export class AdminGuard {
  private router = inject(Router);
  private authService = inject(AuthService);

  async canActivate(): Promise<boolean> {
    // Vérifie si l'utilisateur est authentifié et a le rôle admin
    const isAuthenticated = await firstValueFrom(
      this.authService.isAuthenticated$()
    );

    if (!isAuthenticated) {
      this.router.navigate(['/login']);
      return false;
    }

    const isAdmin = await firstValueFrom(this.authService.hasRole$('ADMIN'));

    if (isAdmin) {
      return true;
    }

    this.router.navigate(['/search']);
    return false;
  }
}
