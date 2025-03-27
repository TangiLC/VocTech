// word-search-input.component.ts
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormControl, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatListModule } from '@angular/material/list';
import { MatError } from '@angular/material/form-field';
import {
  debounceTime,
  distinctUntilChanged,
  filter,
  switchMap,
} from 'rxjs/operators';
import { EMPTY, Observable } from 'rxjs';
import { WordSearchService } from '../../services/wordSearch.service';
import { WordResponse } from '../../dto/wordResponse.dto';
@Component({
  selector: 'app-word-search-input',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatListModule,
  ],
  templateUrl: './wordSearch-input.component.html',
})
export class WordSearchInputComponent {
  private wordPattern = /^[\w\s\-’?]+$/;

  searchControl = new FormControl('', [Validators.pattern(this.wordPattern)]);

  results$!: Observable<WordResponse[]>;

  constructor(private wordSearchService: WordSearchService) {
    this.results$ = this.searchControl.valueChanges.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      filter((value) => this.isValidInput(value)),
      switchMap((value) => {
        if (!value) return EMPTY;
        return this.wordSearchService.searchWords(value);
      })
    );
  }

  isValidInput(value: string | null): boolean {
    return !!value && value.length >= 3 && this.wordPattern.test(value);
  }
}
