import {Component, ElementRef, Inject, ViewChild} from '@angular/core';
import {FormBuilder, FormControl, FormGroup, Validators} from "@angular/forms";
import {LoginCredentials} from "../model/LoginCredentials";
import {UserService} from "../user.service";
import {Router} from "@angular/router";
import {NotificationsService} from "../notifications.service";
import {User} from "../model/User";
import {MatDialog, MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {PasswordConfirmation} from "../model/PasswordConfirmation";
import {UserWithConfirmation} from "../model/UserWithConfirmation";
import {ReCaptcha2Component} from "ngx-captcha";

@Component({
  selector: 'app-login-registration',
  templateUrl: './login-registration.component.html',
  styleUrls: ['./login-registration.component.css']
})
export class LoginRegistrationComponent {
  emailTel: string;
  choices: string[] = ["Email", "Whatsapp"]
  sentCodeLogin = false;
  sentCodeRegister = false;
  sentCodeReset = false;

  protected loginForm : FormGroup;

  registrationForm = new FormGroup({
    email: new FormControl( '',[Validators.required, Validators.email]),
    password: new FormControl('',[Validators.required,Validators.pattern('^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*\\W)(?!.* ).{8,}$')]),
    name: new FormControl('',[Validators.required]),
    surname: new FormControl('',[Validators.required]),
    phoneNumber: new FormControl('', [Validators.required]),
    confirmation: new FormControl('', []),
  });

  passwordResetForm = new FormGroup({
    password: new FormControl('',[Validators.required,Validators.pattern('^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*\\W)(?!.* ).{8,}$')]),
    confirmPassword: new FormControl('',[Validators.required]),
    email: new FormControl( '',[Validators.required, Validators.email]),
    confirmation: new FormControl('', [])
  })
  public captchaIsLoaded = false;
  public captchaSuccess = false;
  public captchaIsExpired = false;
  public captchaResponse?: string;

  public theme: 'light' | 'dark' = 'light';
  public size: 'compact' | 'normal' = 'normal';
  public lang = 'en';
  public type: 'image' | 'audio' = 'image';

  constructor(private userService : UserService,
              private router: Router,
              private notificationService: NotificationsService,
              public dialog: MatDialog, private formBuilder: FormBuilder) {
    this.emailTel = "Tel"
    this.loginForm = this.formBuilder.group({
      recaptcha: ['', Validators.required],
      email: new FormControl( '',[Validators.required, Validators.email]),
      password: new FormControl('',[Validators.required]),
      confirmation: new FormControl('', []),
    });
  }

  handleSuccess({data}: { data: any }) {
    console.log(data);
  }

  selectEmail(): void{
    this.emailTel = "Email"
  }
  selectTel(): void{
    this.emailTel = "Tel"
  }

  resetPassword(): void{
    if (this.passwordResetForm.valid){
      const user : PasswordConfirmation = {
        email: <string>this.passwordResetForm.value.email,
        password: <string>this.passwordResetForm.value.confirmPassword,
        confirmation: "1"
      };
      if(this.emailTel==="Email") {
        this.userService.resetPasswordEmail(user).subscribe({
          next: (result) => {
            this.notificationService.createNotification("Confirm identity!!");
          },
          error: () => {
            this.notificationService.createNotification("Email is not correct!");
          },
        });
      }
      else{
        this.userService.resetPasswordMessage(user).subscribe({
          next: (result) => {
            this.notificationService.createNotification("Confirm identity!!");
          },
          error: () => {
            this.notificationService.createNotification("Email is not correct!");
          },
        });
      }
      this.sentCodeReset = true;
    }
  }

  resetPassword2(): void{
    if (this.passwordResetForm.valid){
      const user : PasswordConfirmation = {
        email: <string>this.passwordResetForm.value.email,
        password: <string>this.passwordResetForm.value.confirmPassword,
        confirmation: <string>this.passwordResetForm.value.confirmation,
      };
      this.userService.resetPassword2(user).subscribe({
        next: (result) => {
          this.notificationService.createNotification("Password reset!");
        },
        error: (error) => {
          if(error.error.includes("past")) {
            this.notificationService.createNotification("The password is the same as one of the previous ones!")
          }
          else {
            this.notificationService.createNotification("Email is not correct!");
          }
        },
      });
    }
    this.sentCodeReset = false;
  }

  /*openDialog(): void {
    const dialogRef = this.dialog.open(ConfirmDialog, {
      data: { loginRegisterPassword: this.loginRegisterPassword, email: this.email,
        password: this.password, confirmation:this.confirmation,
        go: this.go },
    });

    dialogRef.afterClosed().subscribe(result => {
      console.log('The dialog was closed');
      this.go = result
    });
  }*/

  login1() : void {
    if (this.loginForm.valid) {
      const loginVal : LoginCredentials = {
        email: <string>this.loginForm.value.email,
        password: <string>this.loginForm.value.password,
      };

      if(this.emailTel==="Email"){
        this.userService.loginEmail(loginVal).subscribe({
          next: (result) => {
            this.notificationService.createNotification("Confirm identity!");
          },
          error: (error) => {
            if(error.status == 200) {
              this.notificationService.createNotification("Confirm identity!");
              this.sentCodeLogin = true;
            }
            else {
              this.notificationService.createNotification("Email or password is not correct!");
            }
          },
        });
      }
      else{
        this.userService.loginMessage(loginVal).subscribe({
          next: (result) => {
            this.notificationService.createNotification("Confirm identity!");
          },
          error: (error) => {
            if(error.error.includes("identity")) {
              this.notificationService.createNotification("Confirm identity!");
              this.sentCodeLogin = true;
            }
            else {
              this.notificationService.createNotification("Email or password is not correct!");
            }
          },
        });
      }
    }
  }

  login(): void {
    if (this.loginForm.valid) {
      const loginVal : LoginCredentials = {
        email: <string>this.loginForm.value.email,
        password: <string>this.loginForm.value.password,
        confirmation: <string>this.loginForm.value.confirmation
      };
      this.userService.login(loginVal).subscribe({
        next: (result) => {
          sessionStorage.setItem('user', JSON.stringify(result));
          this.router.navigate(['login']);
        },
        error: (error) => {
          if(error.error.includes("expired")) {
            this.notificationService.createNotification("Password expired! Please change your password in the 'Pasword reset tab.'");
          }
          else {
            this.notificationService.createNotification("Email or password is not correct!");
          }
        },
      });
      this.sentCodeLogin = false;
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
        confirmation: <string>this.registrationForm.value.confirmation
      }
      if(this.emailTel==="Email"){
        this.userService.registerWEmail(user).subscribe( {
          next: () => {
            /*this.notificationService.createNotification("User successfully registered! Confirm user!");
            this.loginRegisterPassword = 2;
            this.email = user.email;
            this.password = user.password;
            this.openDialog()*/
            this.notificationService.createNotification("Confirm identity!")
          },
          error: (error) => {
            this.notificationService.createNotification(error.error);
          }
        });
      }
      else{
        this.userService.registerWMessage(user).subscribe( {
          next: () => {
            /*this.notificationService.createNotification("User successfully registered! Confirm user!");
            this.loginRegisterPassword = 2;
            this.email = user.email;
            this.password = user.password;
            this.openDialog()*/
            this.notificationService.createNotification("Confirm identity!")
          },
          error: (error) => {
            this.notificationService.createNotification(error.error);
          }
        });
      }
      this.sentCodeRegister = true;
    }
  }

  register2() : void {
    if (this.registrationForm.valid) {
      const user: UserWithConfirmation = {
        name: <string>this.registrationForm.value.name,
        surname: <string>this.registrationForm.value.surname,
        phoneNumber: <string>this.registrationForm.value.phoneNumber,
        email: <string>this.registrationForm.value.email,
        password: <string>this.registrationForm.value.password,
        confirmation: <string>this.registrationForm.value.confirmation
      }


      this.userService.register(user).subscribe( {
        next: () => {
          this.notificationService.createNotification("User successfully confirmed!");
        },
        error: (error) => {
          this.notificationService.createNotification(error.error);
        }
      });
      this.sentCodeRegister = false;
    }
  }
}
