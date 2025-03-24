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
    localStorage.setItem('token', response.token);
    localStorage.setItem('roles', JSON.stringify(response.roles));
    localStorage.setItem(
      'user',
      JSON.stringify({
        id: response.id,
        username: response.username,
        email: response.email,
        roles: response.roles,
      })
    );
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
    localStorage.removeItem('token');
    localStorage.removeItem('roles');
    localStorage.removeItem('user');
    this.router.navigate(['/login']);
  }

  hasRole(role: string): boolean {
    const roles = JSON.parse(localStorage.getItem('roles') || '[]');
    return roles.includes(role);
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }
}
