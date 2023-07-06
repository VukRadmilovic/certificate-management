import { Component } from '@angular/core';
import {UserService} from "../../shared/user.service";
import {Router} from "@angular/router";

@Component({
  selector: 'app-admin-navigation',
  templateUrl: './admin-navigation.component.html',
  styleUrls: ['./admin-navigation.component.css']
})
export class AdminNavigationComponent {
  constructor(private userService: UserService, private router: Router) {
  }
  public logout() : void {
    this.userService.logout();
    this.router.navigate(['index']).then();
  }
}
