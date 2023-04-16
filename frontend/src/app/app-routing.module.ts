import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {LoginRegistrationComponent} from "./shared/login-registration/login-registration.component";
import {LoginService} from "./shared/login-guard/login.service";
import {UserMainComponent} from "./user/user-main/user-main.component";
import {UserRequestsComponent} from "./user/user-requests/user-requests.component";

const routes: Routes = [
  {path: 'index', component: LoginRegistrationComponent},
  {path:'user-main', component:UserMainComponent},
  {path:'user-requests', component:UserRequestsComponent},
  {
    path: 'login',
    component: LoginRegistrationComponent,
    canActivate: [LoginService],
  },
  {path: '', redirectTo:'/index', pathMatch: 'full'},
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
