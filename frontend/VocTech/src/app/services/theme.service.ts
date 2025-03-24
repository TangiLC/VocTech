import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Theme } from '../dto/theme.dto';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root',
})
export class ThemeService {
  private http = inject(HttpClient);
  private authService = inject(AuthService);
  private apiUrl = 'http://localhost:8082/voctech/themes';

  fetchThemes(): Observable<Theme[]> {
    const token = this.authService.getToken();
    return this.http.get<Theme[]>(this.apiUrl, {
      headers: { Authorization: `Bearer ${token}` },
    });
  }
}
