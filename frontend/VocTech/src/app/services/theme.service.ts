// theme.service.ts
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { Theme } from '../dto/theme.dto';
import { AuthService } from './auth.service';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private http = inject(HttpClient);
  private authService = inject(AuthService);
  private apiUrl = 'http://localhost:8082/voctech/themes';

  private themesSubject = new BehaviorSubject<Theme[]>([]);
  public themes$ = this.themesSubject.asObservable();

  constructor() {
    this.loadThemes();
  }

  private loadThemes(): void {
    const token = this.authService.getToken();
    this.http
      .get<Theme[]>(this.apiUrl, { headers: { Authorization: `Bearer ${token}` } })
      .subscribe({
        next: themes => this.themesSubject.next(themes),
        error: err => console.error('Échec du chargement des thèmes', err),
      });
  }

  refresh(): void {
    this.loadThemes();
  }
}
