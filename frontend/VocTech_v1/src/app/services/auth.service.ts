import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import { encodeTries, decodeTries } from '../utils/remaining'; 

export interface User {
  id: number;
  username: string;
  role: string;
}

export interface JwtResponse {
  token: string;
  id: number;
  username: string;
  role: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly apiUrl = 'http://localhost:8082/api/auth';

  private tokenSubject = new BehaviorSubject<string | null>(
    localStorage.getItem('token')
  );
  public token$ = this.tokenSubject.asObservable();

  private userSubject = new BehaviorSubject<User | null>(
    this.initializeUserFromStorage()
  );
  public user$ = this.userSubject.asObservable();

  private remainingQueriesSubject = new BehaviorSubject<number>(
    decodeTries(localStorage.getItem('remainQ')) ?? 0
  );
  public remainingQueries$ = this.remainingQueriesSubject.asObservable();

  constructor(private http: HttpClient, private router: Router) {}

  private initializeUserFromStorage(): User | null {
    const token = localStorage.getItem('token');
    const userStorage = localStorage.getItem('user');

    if (!token || !userStorage) {
      return null;
    }

    try {
      const userFromStorage = JSON.parse(userStorage);
      const payload = this.decodeJwtPayload(token);

      if (!payload || payload.exp * 1000 <= Date.now()) {
        this.cleanupStorage();
        return null;
      }

      const formatRole = (r: string) => r.replace(/^ROLE_/, '');
      return {
        id: userFromStorage.id,
        username: userFromStorage.username,
        role: formatRole(payload.role || 'GUEST'),
      };
    } catch (error) {
      console.error("Erreur lors de l'initialisation de l'utilisateur", error);
      this.cleanupStorage();
      return null;
    }
  }

  private cleanupStorage(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    localStorage.removeItem('remainQ');
    this.remainingQueriesSubject.next(0);
  }

  /**
   * 🔐 Login + initialisation remainingQueries
   */
  login(username: string, password: string): Observable<JwtResponse> {
    return this.http
      .post<JwtResponse>(`${this.apiUrl}/login`, { username, password })
      .pipe(
        tap((resp) => {
          localStorage.setItem('token', resp.token);

          const remaining = 6;
          localStorage.setItem('remainQ', encodeTries(remaining));
          this.remainingQueriesSubject.next(remaining);

          this.tokenSubject.next(resp.token);

          const formatRole = (r: string) => r.replace(/^ROLE_/, '');
          const usr: User = {
            id: resp.id,
            username: resp.username,
            role: formatRole(resp.role),
          };

          const { role, ...usrForStorage } = usr;
          localStorage.setItem('user', JSON.stringify(usrForStorage));
          this.userSubject.next(usr);
        })
      );
  }

  logout(): void {
    this.cleanupStorage();
    this.tokenSubject.next(null);
    this.userSubject.next(null);
    this.router.navigate(['/login']);
  }

  isAuthenticated$(): Observable<boolean> {
    return this.token$.pipe(
      map((token) => {
        if (!token) return false;
        const payload = this.decodeJwtPayload(token);
        return !!payload && payload.exp! * 1000 > Date.now();
      })
    );
  }

  currentUser$(): Observable<User | null> {
    return this.user$;
  }

  hasRole$(role: string): Observable<boolean> {
    return this.user$.pipe(map((user) => !!user && user.role === role));
  }

  private decodeJwtPayload(token: string): any {
    try {
      const payloadBase64 = token.split('.')[1];
      const decoded = atob(payloadBase64.replace(/-/g, '+').replace(/_/g, '/'));
      return JSON.parse(decoded);
    } catch (error) {
      console.error('Erreur décodage JWT', error);
      return null;
    }
  }
  remainingQueries(): Observable<number> {
    return this.remainingQueries$;
  }

  updateRemainingQueries(newValue: number): void {
    if (newValue<0){newValue=0}
    localStorage.setItem('remainQ', newValue===0?'000000':encodeTries(newValue));
    this.remainingQueriesSubject.next(newValue);
  }
}
