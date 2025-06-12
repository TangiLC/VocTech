// word-search.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { AuthService } from './auth.service';
import { Observable, tap } from 'rxjs';
import { WordResponse } from '../dto/wordResponse.dto';

@Injectable({
  providedIn: 'root',
})
export class WordSearchService {
  private apiUrl = '/api/voctech';

  constructor(private http: HttpClient, private authService: AuthService) {}

  searchWords(query: string): Observable<WordResponse[]> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);
    return this.http
      .get<WordResponse[]>(`${this.apiUrl}/search?word=${encodeURIComponent(query)}`, {
        headers,
      })
      .pipe(
        tap((response) => {
          console.log('Réponse API:', response);
        })
      );
  }


  getAllWords(): Observable<WordResponse[]> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);
    return this.http
      .get<WordResponse[]>(`${this.apiUrl}/words`, { headers })
      .pipe(tap(response => console.log('Réponse API (all words):', response)));
  }
}
