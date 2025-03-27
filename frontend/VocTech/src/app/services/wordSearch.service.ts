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
  private apiUrl = '/voctech/search';

  constructor(private http: HttpClient, private authService: AuthService) {}

  searchWords(query: string): Observable<WordResponse[]> {
    const token = this.authService.getToken();
    const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);
    return this.http
      .get<WordResponse[]>(`${this.apiUrl}?word=${encodeURIComponent(query)}`, {
        headers,
      })
      .pipe(
        tap((response) => {
          console.log('Réponse API:', response);
        })
      );
  }
}
