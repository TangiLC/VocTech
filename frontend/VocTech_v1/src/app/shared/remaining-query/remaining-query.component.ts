import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService, User } from '../../services/auth.service';
import { LanguageService } from '../../services/language.service';
import { decodeTries } from '../../utils/remaining';
import { Observable, combineLatest, map } from 'rxjs';

@Component({
  selector: 'app-remaining-query',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './remaining-query.component.html',
  styleUrls: ['./remaining-query.component.scss'],
})
export class RemainingQueryComponent {
  private authService = inject(AuthService);
  private languageService = inject(LanguageService);

  user$: Observable<User | null> = this.authService.currentUser$();
  lang$: Observable<'fr' | 'en'> = this.languageService.language$;

  role$: Observable<string> = this.user$.pipe(
    map((user) => user?.role ?? 'GUEST')
  );

  remainingQueries: number = 0;

  mssg: Record<'fr' | 'en', string[]> = {
    fr: [
      'Vous êtes abonné',
      'Vous utilisez la version gratuite, il vous reste',
      'requête',
      'disponible',
      'Cliquez sur ce lien pour vous abonner.',
    ],
    en: [
      'You are subscribed',
      'You are using the free version. You have',
      'request',
      'left',
      'Click this link to subscribe.',
    ],
  };

  constructor() {
    const encoded = localStorage.getItem('remainQ');
    this.remainingQueries = encoded ? decodeTries(encoded) : 0;
  }
}
