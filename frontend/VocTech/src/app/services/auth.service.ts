import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { JwtResponse } from '../dto/auth.dto';
import { User } from '../dto/user.dto';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private apiUrl = 'http://localhost:8082/auth';

  private http = inject(HttpClient);
  private router = inject(Router);

  login(username: string, password: string): Observable<JwtResponse> {
    return this.http
      .post<JwtResponse>(`${this.apiUrl}/login`, {
        username,
        password,
      })
      .pipe(
        tap((response) => {
          this.saveAuthData(response);
        })
      );
  }

  register(user: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/register`, user);
  }

  saveAuthData(response: JwtResponse): void {
    // Stocke le token JWT
    localStorage.setItem('token', response.token);

    // Stocke toutes les informations utilisateur (y compris les rôles) dans un seul endroit
    const userData = {
      id: response.id,
      username: response.username,
      email: response.email,
      roles: response.roles,
    };
    localStorage.setItem('user', JSON.stringify(userData));
  }

  getCurrentUser(): User | null {
    const userStr = localStorage.getItem('user');
    if (!userStr) {
      return null;
    }

    try {
      return JSON.parse(userStr) as User;
    } catch (e) {
      console.error('Error parsing user data', e);
      return null;
    }
  }

  isAuthenticated(): boolean {
    return !!localStorage.getItem('token');
  }

  isAdmin(): boolean {
    return this.hasRole('ADMIN');
  }

  logout(): void {
    // Supprime toutes les données d'authentification
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    this.router.navigate(['/login']);
  }

  hasRole(role: string): boolean {
    const user = this.getCurrentUser();
    if (!user || !user.roles) {
      return false;
    }
    console.log(user,user.roles.includes(role))
    return user.roles.includes(role);
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }
}
