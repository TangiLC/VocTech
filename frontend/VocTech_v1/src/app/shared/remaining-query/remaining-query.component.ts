import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService, User } from '../../services/auth.service';
import { LanguageService } from '../../services/language.service';
import { Observable, map } from 'rxjs';

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
  remainingQueries$: Observable<number> = this.authService.remainingQueries();

  role$: Observable<string> = this.user$.pipe(
    map((user) => user?.role ?? 'GUEST')
  );

  mssg: Record<'fr' | 'en', string[]> = {
    fr: [
      'Vous êtes abonné',
      'Vous utilisez la version gratuite, il vous reste',
      'requête','requêtes',
      '',
      'Vous avez épuisé les requêtes de la version gratuite',
      'Cliquez sur ce lien pour vous abonner.',
    ],
    en: [
      'You are subscribed',
      'You are using the free version. You have',
      'request','requests',
      'left',
      'You have reached the limit of the free version',
      'Click this link to subscribe.',
    ],
  };
}
