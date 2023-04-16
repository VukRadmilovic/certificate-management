import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppComponent } from './app.component';
import {RouterOutlet} from "@angular/router";
import { LoginRegistrationComponent } from './shared/login-registration/login-registration.component';
import {AppRoutingModule} from "./app-routing.module";
import {InterceptorService} from "./shared/interceptor/interceptor.service";
import {HTTP_INTERCEPTORS, HttpClientModule} from "@angular/common/http";
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import {MatTabsModule} from "@angular/material/tabs";
import {MatInputModule} from "@angular/material/input";
import {ReactiveFormsModule} from "@angular/forms";
import {MatButtonModule} from "@angular/material/button";
import {MatSnackBarModule} from "@angular/material/snack-bar";
import { UserNavigationComponent } from './user/user-navigation/user-navigation.component';
import { UserMainComponent } from './user/user-main/user-main.component';
import { UserRequestsComponent } from './user/user-requests/user-requests.component';
import {MatTableModule} from "@angular/material/table";
import {MatPaginatorModule} from "@angular/material/paginator";
import {JwtHelperService} from "@auth0/angular-jwt";
import { RequestDenyReasonDialogComponent } from './user/request-deny-reason-dialog/request-deny-reason-dialog.component';
import {MatDialogModule} from "@angular/material/dialog";

@NgModule({
  declarations: [
    AppComponent,
    LoginRegistrationComponent,
    UserNavigationComponent,
    UserMainComponent,
    UserRequestsComponent,
    RequestDenyReasonDialogComponent
  ],
  imports: [
    BrowserModule,
    RouterOutlet,
    AppRoutingModule,
    BrowserAnimationsModule,
    MatTabsModule,
    MatInputModule,
    ReactiveFormsModule,
    HttpClientModule,
    MatButtonModule,
    MatSnackBarModule,
    MatTableModule,
    MatPaginatorModule,
    MatDialogModule,
  ],
  providers: [    {
    provide: HTTP_INTERCEPTORS,
    useClass: InterceptorService,
    multi: true,
  },],
  bootstrap: [AppComponent]
})
export class AppModule { }
