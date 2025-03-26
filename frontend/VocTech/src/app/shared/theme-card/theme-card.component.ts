import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import colors from './theme-colors.json';

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
    const color = colors.theme[this.id];
    console.log(this.id,color)
    if (!color) return 'red';
    return `rgba(${color[0]}, ${color[1]}, ${color[2]}, 0.8)`;
  }

  get contentBackground(): string {
    const color = colors.theme[this.id];
    if (!color) return 'transparent';
    return `rgba(${color[0]}, ${color[1]}, ${color[2]}, 0.3)`;
  }
}
