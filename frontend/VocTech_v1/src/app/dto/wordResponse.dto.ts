export interface RelatedWordResponse {
  id: number;
  word: string;
  language: string;
}

export interface WordRelations {
  synonym?: RelatedWordResponse[];
  antonym?: RelatedWordResponse[];
  translation?: RelatedWordResponse[];
}

export interface WordResponse {
  id: number;
  word: string;
  language: string;
  themeId: number[];
  relations: WordRelations;
}
