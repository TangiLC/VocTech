export interface RelatedWordResponse {
  id: number;
  word: string;
  language: string;
}

export interface WordResponse {
  id: number;
  word: string;
  language: string;
  themeId: number[];
  relations: {
    [key: string]: RelatedWordResponse[];
  };
}
