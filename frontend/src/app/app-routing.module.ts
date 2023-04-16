import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {LoginRegistrationComponent} from "./shared/login-registration/login-registration.component";
import {LoginService} from "./shared/login-guard/login.service";

const routes: Routes = [
  {path: 'index', component: LoginRegistrationComponent},
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
