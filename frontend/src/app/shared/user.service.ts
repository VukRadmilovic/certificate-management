import { Injectable } from '@angular/core';
import {LoginCredentials} from "./model/LoginCredentials";
import {Observable, timer} from "rxjs";
import {HttpClient, HttpEvent, HttpHeaders} from "@angular/common/http";
import {Token} from "./model/Token";
import {environment} from "./environments/environment";
import {JwtHelperService} from "@auth0/angular-jwt";
import {MatSnackBar} from "@angular/material/snack-bar";
import {NotificationsService} from "./notifications.service";
import {User} from "./model/User";
import {UserWithConfirmation} from "./model/UserWithConfirmation";
import {PasswordConfirmation} from "./model/PasswordConfirmation";
import {Router} from "@angular/router";

@Injectable({
  providedIn: 'root'
})
export class UserService {

  private headers = new HttpHeaders({
    'Content-Type': 'application/json',
    skip: 'true',
  });
  constructor(private http:HttpClient,
              private snackBar: MatSnackBar,
              private router : Router,
              private notificationService: NotificationsService) { }

  public login(auth: LoginCredentials): Observable<Token> {
    return this.http.post<Token>(environment.apiURL + 'user/login', auth, {
      headers: this.headers,
    });
  }
  public loginEmail(auth: LoginCredentials): Observable<Token> {
    return this.http.post<Token>(environment.apiURL + 'user/login/sendEmail', auth, {
      headers: this.headers,
    });
  }
  public loginMessage(auth: LoginCredentials): Observable<Token> {
    return this.http.post<Token>(environment.apiURL + 'user/login/sendMessage', auth, {
      headers: this.headers,
    });
  }

  public logout(): void {
    sessionStorage.removeItem('user');
  }

  public forceLogout() : void {
    if(sessionStorage.getItem('user') == null) return;
    this.notificationService.createNotification("Session expired. You will be redirected to the login page.");
    timer(5000).subscribe(x => {
      if(sessionStorage.getItem('user') == null) return;
      this.logout();
      this.router.navigate(['index'])});
  }

  public register(user : UserWithConfirmation) : Observable<ArrayBuffer> {
    const options: any = {
      responseType: 'text',
      headers:this.headers
    };
    return this.http.post(environment.apiURL + 'user/register', user, options);
  }

  public resetPasswordEmail(user : PasswordConfirmation): Observable<ArrayBuffer> {
    const options: any = {
      responseType: 'text',
      headers:this.headers
    };
    return this.http.post(environment.apiURL + 'user/passwordReset/sendEmail', user, options);
  }
  public resetPasswordMessage(user : PasswordConfirmation): Observable<ArrayBuffer> {
    const options: any = {
      responseType: 'text',
      headers:this.headers
    };
    return this.http.post(environment.apiURL + 'user/passwordReset/sendMessage', user, options);
  }

  public resetPassword2(user : PasswordConfirmation): Observable<ArrayBuffer> {
    const options: any = {
      responseType: 'text',
      headers:this.headers
    };
    return this.http.post(environment.apiURL + 'user/passwordReset', user, options);
  }

  public registerWEmail(user : User) : Observable<ArrayBuffer> {
    const options: any = {
      responseType: 'text',
      headers:this.headers
    };
    return this.http.post(environment.apiURL + 'user/register/wEmail', user, options);
  }
  public registerWMessage(user : User) : Observable<ArrayBuffer> {
    const options: any = {
      responseType: 'text',
      headers:this.headers
    };
    return this.http.post(environment.apiURL + 'user/register/wMessage', user, options);
  }

  public isLoggedIn(): boolean {
    if (sessionStorage.getItem('user') != null) {
      return true;
    }
    return false;
  }

  public getRole(): any {
    if (this.isLoggedIn()) {
      const loginInfo = sessionStorage.getItem('user');
      if(loginInfo == null) return null;
      const accessToken: Token = JSON.parse(loginInfo);
      const helper = new JwtHelperService();
      try {
        const role = helper.decodeToken(accessToken.accessToken).role;
        return role;
      }
      catch (err : any){
        this.notificationService.createNotification("Email or password is not correct!");
        sessionStorage.removeItem("user");
        return null;
      }
    }
  }
}
