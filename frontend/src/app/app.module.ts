import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppComponent } from './app.component';
import {RouterOutlet} from "@angular/router";
import { LoginRegistrationComponent } from './shared/login-registration/login-registration.component';
import { ConfirmDialog } from './shared/confirm-dialog/confirm-dialog';
import {AppRoutingModule} from "./app-routing.module";
import {InterceptorService} from "./shared/interceptor/interceptor.service";
import {HTTP_INTERCEPTORS, HttpClientModule} from "@angular/common/http";
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import {MatTabsModule} from "@angular/material/tabs";
import {MatInputModule} from "@angular/material/input";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
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
import { ReceivedRequestsComponent } from './user/received-requests/received-requests.component';
import {MatSortModule} from "@angular/material/sort";
import { NewRequestFormComponent } from './user/new-request-form/new-request-form.component';
import {MatRadioModule} from "@angular/material/radio";
import {MatSelectModule} from "@angular/material/select";
import {MatDatepickerModule} from "@angular/material/datepicker";
import {MatNativeDateModule} from "@angular/material/core";
import { AdminNavigationComponent } from './admin/admin-navigation/admin-navigation.component';
import { AllRequestsComponent } from './admin/all-requests/all-requests.component';
import { AdminMainComponent } from './admin/admin-main/admin-main.component';
import { AdminReceivedRequestsComponent } from './admin/admin-received-requests/admin-received-requests.component';
import { UserReceivedRequestsComponent } from './user/user-received-requests/user-received-requests.component';
import { AdminNewRequestComponent } from './admin/admin-new-request/admin-new-request.component';
import { CertificatesComponent } from './shared/certificates/certificates.component';
import {NgxFileDropModule} from "ngx-file-drop";
import { WithdrawReasonDialogComponent } from './user/withdraw-reason-dialog/withdraw-reason-dialog.component';
import {MatCheckboxModule} from "@angular/material/checkbox";
import { NgxCaptchaModule } from 'ngx-captcha';
import {MatCardModule} from "@angular/material/card";

@NgModule({
  declarations: [
    AppComponent,
    ConfirmDialog,
    LoginRegistrationComponent,
    UserNavigationComponent,
    UserMainComponent,
    UserRequestsComponent,
    RequestDenyReasonDialogComponent,
    ReceivedRequestsComponent,
    NewRequestFormComponent,
    AdminNavigationComponent,
    AllRequestsComponent,
    AdminMainComponent,
    AdminReceivedRequestsComponent,
    UserReceivedRequestsComponent,
    AdminNewRequestComponent,
    CertificatesComponent,
    WithdrawReasonDialogComponent
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
    MatSortModule,
    MatRadioModule,
    FormsModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    NgxFileDropModule,
    MatCheckboxModule,
    NgxCaptchaModule,
    MatCardModule
  ],
  providers: [    {
    provide: HTTP_INTERCEPTORS,
    useClass: InterceptorService,
    multi: true,
  },],
  bootstrap: [AppComponent]
})
export class AppModule { }
