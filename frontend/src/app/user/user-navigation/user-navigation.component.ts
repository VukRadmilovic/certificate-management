import { Component } from '@angular/core';
import {UserService} from "../../shared/user.service";
import {Router} from "@angular/router";

@Component({
  selector: 'app-user-navigation',
  templateUrl: './user-navigation.component.html',
  styleUrls: ['./user-navigation.component.css']
})
export class UserNavigationComponent {

  constructor(private userService: UserService, private router: Router) {
  }
  public logout() : void {
    this.userService.logout();
    this.router.navigate(['index']).then();
  }
}
