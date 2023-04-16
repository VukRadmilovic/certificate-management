import { Injectable } from '@angular/core';
import {LoginCredentials} from "./model/LoginCredentials";
import {Observable} from "rxjs";
import {HttpClient, HttpEvent, HttpHeaders} from "@angular/common/http";
import {Token} from "./model/Token";
import {environment} from "./environments/environment";
import {JwtHelperService} from "@auth0/angular-jwt";
import {MatSnackBar} from "@angular/material/snack-bar";
import {NotificationsService} from "./notifications.service";
import {User} from "./model/User";

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
              private notificationService: NotificationsService) { }

  public login(auth: LoginCredentials): Observable<Token> {
    return this.http.post<Token>(environment.apiURL + 'user/login', auth, {
      headers: this.headers,
    });
  }

  public logout(): void {
    sessionStorage.removeItem('user');
  }

  public register(user : User) : Observable<ArrayBuffer> {
    const options: any = {
      responseType: 'text',
      headers:this.headers
    };
    return this.http.post(environment.apiURL + 'user/register', user, options);
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
