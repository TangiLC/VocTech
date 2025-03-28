import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { WordResponse } from './../../dto/wordResponse.dto';
import { RelatedWordResponse } from './../../dto/wordResponse.dto';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-word-table',
  standalone: true,
  imports: [CommonModule, MatTableModule],
  templateUrl: './wordsTable.component.html',
  styleUrls: ['./wordsTable.component.scss'],
})
export class WordsTableComponent implements OnInit {
  @Input() data$!: Observable<WordResponse[]>;
  displayedColumns: string[] = [
    'word',
    'language',
    'synonyms',
    'translations',
    'translationLanguages',
  ];
  dataSource: WordResponse[] = [];

  ngOnInit() {
    this.data$.subscribe((data) => {
      console.log('Received data:', data);
      this.dataSource = data;
    });
  }

  getSynonyms(word: WordResponse): string {
    //console.log('Word for synonyms:', word);
    //console.log('Synonyms:', word.relations?.synonym);

    const synonyms = word.relations?.synonym?.map((s) => s.word);
    //console.log('Mapped synonyms:', synonyms);

    return synonyms?.length ? synonyms.join(', ') : '-';
  }

  getTranslations(word: WordResponse): string {
    //console.log('Word for translations:', word);
    //console.log('Translations:', word.relations?.translation);

    const translations = word.relations?.translation?.map((t) => t.word);
    //console.log('Mapped translations:', translations);

    return translations?.length ? translations.join(', ') : '-';
  }

  getTranslationLanguages(word: WordResponse): string {
    //console.log('Word for translation languages:', word);
    //console.log('Translations:', word.relations?.translation);

    const langs = new Set(
      word.relations?.translation?.map((t) => t.language) || []
    );
    //console.log('Translation languages:', langs);

    return langs.size ? Array.from(langs).join(', ') : '-';
  }
}
