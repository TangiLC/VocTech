import { WordsTableComponent } from './wordsTable.component';
import { ComponentFixture, TestBed } from '@angular/core/testing';

describe('WordSearchTableComponent', () => {
  let component: WordsTableComponent;
  let fixture: ComponentFixture<WordsTableComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [WordsTableComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(WordsTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
