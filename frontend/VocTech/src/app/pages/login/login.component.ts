import { CommonModule } from '@angular/common';
import { Component, OnInit, OnDestroy } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  Validators,
  ReactiveFormsModule,
} from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';

import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';

import { LanguageService } from '../../services/language.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  imports: [
    CommonModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    ReactiveFormsModule,
    MatCardModule,
  ],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
})
export class LoginComponent implements OnInit, OnDestroy {
  loginForm: FormGroup;
  loading = false;
  errorMessage: string = '';
  currentLanguage: 'en' | 'fr';
  private languageSubscription: Subscription;

  labels = {
    en: {
      username: 'Username',
      password: 'Password',
      login: 'Connect to VocTech',
      credentialErrorMessage: 'Invalid username or password!',
    },
    fr: {
      username: "Nom d'utilisateur",
      password: 'Mot de passe',
      login: 'Se connecter à VocTech',
      credentialErrorMessage: "Nom d'utilisateur ou mot de passe invalide !",
    },
  };

  constructor(
    private fb: FormBuilder,
    private http: HttpClient,
    private router: Router,
    private languageService: LanguageService,
    private authService: AuthService
  ) {
    this.loginForm = this.fb.group({
      username: ['', Validators.required],
      password: ['', Validators.required],
    });
    this.currentLanguage = this.languageService.getCurrentLanguage();
    this.languageSubscription = this.languageService.language$.subscribe(
      (lang) => {
        this.currentLanguage = lang;
      }
    );
  }

  ngOnInit() {
    // Abonnement aux changements de langue
    this.languageSubscription = this.languageService.language$.subscribe(
      (lang) => {
        this.currentLanguage = lang;
      }
    );

    // Initialisation avec la langue courante du service
    this.currentLanguage = this.languageService.getCurrentLanguage();
  }

  ngOnDestroy() {
    // Désabonnement pour éviter les fuites de mémoire
    this.languageSubscription?.unsubscribe();
  }

  onSubmit() {
    if (this.loginForm.invalid) return;

    this.loading = true;
    this.errorMessage = '';

    this.authService
      .login(
        this.loginForm.get('username')?.value,
        this.loginForm.get('password')?.value
      )
      .subscribe({
        next: (response) => {
          this.router.navigate(['/theme']); // Redirection vers la page d'accueil
        },
        error: () => {
          this.errorMessage =
            this.labels[this.currentLanguage].credentialErrorMessage;
          this.loading = false;
        },
      });
  }
}
