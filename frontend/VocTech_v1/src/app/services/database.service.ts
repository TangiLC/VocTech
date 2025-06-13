import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Theme } from '../dto/theme.dto';
import { AuthService } from './auth.service';

const API_BASE = 'http://localhost:8082';

@Injectable({ providedIn: 'root' })
export class DatabaseService {
  private http = inject(HttpClient);
  private authService = inject(AuthService);

  /**
   * Construit les headers avec le token d'authentification
   */
  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');

    return new HttpHeaders({ Authorization: `Bearer ${token}` });
  }

  /** Ajoute une paire de mots (translation/antonym) */
  addPair(
    entries: Array<{ word: string; language: string; themeId: number[] }>,
    relation: string
  ): Observable<any> {
    const payload = { entries, relation };
    return this.http.post(`${API_BASE}/api/database/addpair`, payload, {
      headers: this.getHeaders(),
    });
  }

  /** Recherche un mot existant par sa chaîne */
  searchWord(word: string): Observable<{ id: number }[]> {
    const params = new HttpParams().set('word', word);
    return this.http.get<{ id: number }[]>(`${API_BASE}/api/voctech/search`, {
      params,
      headers: this.getHeaders(),
    });
  }

  /** Ajoute un synonyme via relation PATCH */
  addSynonym(
    sourceId: number,
    target: { word: string; language: string; themeId: number[] }
  ): Observable<any> {
    const body = { source: { id: sourceId }, target, relation: 'synonym' };
    return this.http.patch(`${API_BASE}/api/database/addword`, body, {
      headers: this.getHeaders(),
    });
  }
}
