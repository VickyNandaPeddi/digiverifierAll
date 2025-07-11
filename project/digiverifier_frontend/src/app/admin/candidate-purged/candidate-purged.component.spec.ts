import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CandidatePurgedComponent } from './candidate-purged.component';

describe('CandidatePurgedComponent', () => {
  let component: CandidatePurgedComponent;
  let fixture: ComponentFixture<CandidatePurgedComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CandidatePurgedComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidatePurgedComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
