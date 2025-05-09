import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { MatToolbarModule } from '@angular/material/toolbar';
import { RouterModule } from '@angular/router';
import { LanguageService } from '../services/language.service';

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [CommonModule, MatToolbarModule, RouterModule],
  templateUrl: './footer.component.html',
  styleUrls: ['./footer.component.scss'],
})
export class FooterComponent {
  languageService = inject(LanguageService);
  disclaimerText = {
    fr: 'retrouvez la version papier en suivant ce lien',
    en: 'find the paper version by following this link',
  };

  readonly imageUrl =
    'https://static.wixstatic.com/media/26a78d_5fc48e5c5b0243c698996428b5d7db76~mv2.jpg';
  readonly linkUrl =
    'https://www.editions-fedora.com/product-page/copy-of-vocabulaire-technique-d-arch%C3%A9ologie-fr-ang-1';
}
