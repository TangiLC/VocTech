import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { WordSearchInputComponent } from '../wordSearch-input/wordSearch-input.component';
import { WordsTableComponent } from '../words-table/wordsTable.component';
import { RemainingQueryComponent } from '../remaining-query/remaining-query.component';
import bgColors from './theme-colors.json';
import { Observable } from 'rxjs';
import { WordResponse } from '../../dto/wordResponse.dto';

@Component({
  selector: 'app-theme-card',
  templateUrl: './theme-card.component.html',
  styleUrls: ['./theme-card.component.scss'],
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    WordSearchInputComponent,
    WordsTableComponent,
    //RemainingQueryComponent,
  ],
})
export class ThemeCardComponent {
  @Input() id: number = 0;
  @Input() title: string = '';
  @Input() content: string = '';
  results$!: Observable<WordResponse[]>;

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
