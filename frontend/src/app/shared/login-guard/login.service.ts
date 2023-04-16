import { Injectable } from '@angular/core';
import {CanActivate, Router, UrlTree} from "@angular/router";
import {Observable} from "rxjs";
import {UserService} from "../user.service";

@Injectable({
  providedIn: 'root'
})
export class LoginService implements CanActivate {

  constructor(private router: Router, private userService: UserService) {}
  canActivate():
    | Observable<boolean | UrlTree>
    | Promise<boolean | UrlTree>
    | boolean
    | UrlTree {
    if (this.userService.isLoggedIn()) {
      const role  = this.userService.getRole();
      if(role == 'ROLE_AUTHENTICATED') {
        console.log("authenticated");
      }
      if(role == 'ROLE_ADMIN') {
        console.log("admin");
      }
    }
    return false;
  }
}
