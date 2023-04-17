import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {LoginRegistrationComponent} from "./shared/login-registration/login-registration.component";
import {LoginService} from "./shared/login-guard/login.service";
import {UserMainComponent} from "./user/user-main/user-main.component";
import {UserRequestsComponent} from "./user/user-requests/user-requests.component";
import {ReceivedRequestsComponent} from "./user/received-requests/received-requests.component";
import {NewRequestFormComponent} from "./user/new-request-form/new-request-form.component";
import {AllRequestsComponent} from "./admin/all-requests/all-requests.component";
import {AdminMainComponent} from "./admin/admin-main/admin-main.component";
import {AdminReceivedRequestsComponent} from "./admin/admin-received-requests/admin-received-requests.component";
import {UserReceivedRequestsComponent} from "./user/user-received-requests/user-received-requests.component";

const routes: Routes = [
  {path: 'index', component: LoginRegistrationComponent},
  {path:'user-main', component:UserMainComponent},
  {path:'user-requests', component:UserRequestsComponent},
  {path:'user-received-requests', component:UserReceivedRequestsComponent},
  {path:'admin-received-requests', component:AdminReceivedRequestsComponent},
  {path:'new-request', component:NewRequestFormComponent},
  {path:'all-requests', component:AllRequestsComponent},
  {path:'admin-main', component:AdminMainComponent},
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
