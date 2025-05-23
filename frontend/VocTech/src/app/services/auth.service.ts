import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable } from 'rxjs';
import { map, tap } from 'rxjs/operators';

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
  private readonly apiUrl = 'http://localhost:8082/auth';

  // Subjects maintenus en mémoire et initialisés depuis le localStorage
  private tokenSubject = new BehaviorSubject<string | null>(
    localStorage.getItem('token')
  );
  public token$ = this.tokenSubject.asObservable();

  private userSubject = new BehaviorSubject<User | null>(
    JSON.parse(localStorage.getItem('user') || 'null')
  );
  public user$ = this.userSubject.asObservable();

  constructor(private http: HttpClient, private router: Router) {}

  /**
   * Envoi des identifiants, stockage du token et de l'utilisateur
   */
  login(username: string, password: string): Observable<JwtResponse> {
    return this.http
      .post<JwtResponse>(`${this.apiUrl}/login`, { username, password })
      .pipe(
        tap((resp) => {
          // Stockage dans le localStorage
          localStorage.setItem('token', resp.token);

          this.tokenSubject.next(resp.token);

          // Construction de l'objet User et stockage
          const usr: User = {
            id: resp.id,
            username: resp.username,
            role: resp.role,
          };
          this.userSubject.next(usr);

          const { role, ...usrForStorage } = usr;
          localStorage.setItem('user', JSON.stringify(usrForStorage));
          this.userSubject.next(usr);
        })
      );
  }

  /**
   * Déconnexion : nettoyage et redirection
   */
  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    this.tokenSubject.next(null);
    this.userSubject.next(null);
    this.router.navigate(['/login']);
  }

  /**
   * Indique si un token valide est présent
   */
  isAuthenticated$(): Observable<boolean> {
    return this.token$.pipe(
      map((token) => {
        if (!token) {
          return false;
        }
        const payload = this.decodeJwtPayload(token);
        return !!payload && payload.exp! * 1000 > Date.now();
      })
    );
  }

  /**
   * Expose l'utilisateur courant
   */
  currentUser$(): Observable<User | null> {
    return this.user$;
  }

  /**
   * Vérifie si l'utilisateur a un rôle donné
   */
  hasRole$(role: string): Observable<boolean> {
    return this.user$.pipe(map((user) => !!user && user.role === role));
  }

  /**
   * Décodage du payload JWT (base64)
   */
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
}
