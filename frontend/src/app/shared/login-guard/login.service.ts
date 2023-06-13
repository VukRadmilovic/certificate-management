import { Injectable } from '@angular/core';
import {CanActivate, Router, UrlTree} from "@angular/router";
import {Observable, timer} from "rxjs";
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
      //BEST PRACTICE JE 30 MINUTA !! (1800000 milisekundi)
      timer(60000).subscribe(x => this.userService.forceLogout());
      if(role == 'ROLE_AUTHENTICATED') {
        this.router.navigate(['user-main'])
      }
      if(role == 'ROLE_ADMIN') {
        this.router.navigate(['admin-main'])
      }
    }
    return false;
  }
}
