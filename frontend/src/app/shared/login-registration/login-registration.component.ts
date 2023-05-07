import { Component } from '@angular/core';
import {FormControl, FormGroup, Validators} from "@angular/forms";
import {LoginCredentials} from "../model/LoginCredentials";
import {UserService} from "../user.service";
import {Router} from "@angular/router";
import {NotificationsService} from "../notifications.service";
import {User} from "../model/User";

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

  registrationForm = new FormGroup({
    email: new FormControl( '',[Validators.required, Validators.email]),
    password: new FormControl('',[Validators.required,Validators.pattern('^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*\\W)(?!.* ).{8,}$')]),
    name: new FormControl('',[Validators.required]),
    surname: new FormControl('',[Validators.required]),
    phoneNumber: new FormControl('', [Validators.required]),
  });

  constructor(private userService : UserService,
              private router: Router,
              private notificationService: NotificationsService) {
  }

  login() : void {
    if (this.loginForm.valid) {
      const loginVal : LoginCredentials = {
        email: <string>this.loginForm.value.email,
        password: <string>this.loginForm.value.password,
      };
      this.userService.login(loginVal).subscribe({
        next: (result) => {
          sessionStorage.setItem('user', JSON.stringify(result));
          this.router.navigate(['login']);
        },
        error: () => {
          this.notificationService.createNotification("Email or password is not correct!");
        },
      });
    }
  }

  register() : void {
    if (this.registrationForm.valid) {
      const user: User = {
        name: <string>this.registrationForm.value.name,
        surname: <string>this.registrationForm.value.surname,
        phoneNumber: <string>this.registrationForm.value.phoneNumber,
        email: <string>this.registrationForm.value.email,
        password: <string>this.registrationForm.value.password,
      }


      this.userService.register(user).subscribe( {
        next: () => {
          this.notificationService.createNotification("User successfully registered!");
        },
        error: (error) => {
          this.notificationService.createNotification(error.error);
        }
    });
    }
  }
}
