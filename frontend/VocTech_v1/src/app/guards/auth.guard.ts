// auth.guard.ts
import { Injectable, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { firstValueFrom } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class AuthGuard {
  private router = inject(Router);
  private authService = inject(AuthService);

  async canActivate(): Promise<boolean> {
  const isAuth = await firstValueFrom(this.authService.isAuthenticated$());
  if (isAuth) {
    return true;
  }
  this.router.navigate(['/login']);
  return false;
}

}
