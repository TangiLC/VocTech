import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { LanguageService } from '../../services/language.service';
import { AuthService } from '../../services/auth.service';
import { encodeTries } from '../../utils/remaining';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-subscribe',
  standalone: true,
  imports: [CommonModule, MatButtonModule, RouterModule],
  templateUrl: './subscribe.component.html',
  styleUrls: ['./subscribe.component.scss'],
})
export class SubscribeComponent {
  languageService = inject(LanguageService);
  authService = inject(AuthService);

  readonly title = { fr: 'Abonnement', en: 'Subscription' };

  readonly message = {
    fr: [
      "Cette fonction n'est pas encore disponible,",
      'cliquez ici',
      'pour rafraîchir la page',
    ],
    en: [
      'This feature is not available yet,',
      'click here',
      'to refresh the page',
    ],
  };

  constructor(private router: Router) {}

  onClick(): void {
    this.authService.updateRemainingQueries(10);
    this.router.navigate(['/search']);
  }
}
