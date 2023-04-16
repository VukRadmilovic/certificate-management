import { Component } from '@angular/core';
import {FormControl, FormGroup, Validators} from "@angular/forms";

@Component({
  selector: 'app-login-registration',
  templateUrl: './login-registration.component.html',
  styleUrls: ['./login-registration.component.css']
})
export class LoginRegistrationComponent {
  loginForm = new FormGroup({
    email: new FormControl( '',[Validators.required, Validators.email]),
    password: new FormControl('',[Validators.required]),
  });
}
