import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import bgColors from './theme-colors.json';

@Component({
  selector: 'app-theme-card',
  templateUrl: './theme-card.component.html',
  styleUrls: ['./theme-card.component.scss'],
  standalone: true,
  imports: [CommonModule, MatCardModule],
})
export class ThemeCardComponent {
  @Input() id: number = 0;
  @Input() title: string = '';
  @Input() content: string = '';

  get titleBackground(): string {
    const rgb = bgColors.theme[this.id];
    if (!rgb) return 'transparent';
    return `rgba(${rgb[0]}, ${rgb[1]}, ${rgb[2]}, 0.8)`;
  }

  get contentBackground(): string {
    const rgb = bgColors.theme[this.id];
    if (!rgb) return 'transparent';
    return `rgba(${rgb[0]}, ${rgb[1]}, ${rgb[2]}, 0.3)`;
  }
}
